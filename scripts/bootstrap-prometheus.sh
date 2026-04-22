#!/usr/bin/env bash
# Brings up the Prometheus monitoring slice of the DevSecOps stack
# (Jenkins + Prometheus + Grafana) and verifies that every scrape
# target reports as up before exiting.
#
# Usage: scripts/bootstrap-prometheus.sh
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.devsecops.yml}"
JENKINS_URL="${JENKINS_URL:-http://localhost:8080}"
PROM_URL="${PROM_URL:-http://localhost:9090}"
GRAFANA_URL="${GRAFANA_URL:-http://localhost:3000}"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-300}"

green() { printf '\033[0;32m%s\033[0m\n' "$*"; }
red()   { printf '\033[0;31m%s\033[0m\n' "$*" >&2; }
info()  { printf '[bootstrap] %s\n' "$*"; }

wait_for_http() {
  local url="$1" label="$2" deadline=$((SECONDS + WAIT_TIMEOUT))
  info "Waiting for ${label} at ${url}"
  while (( SECONDS < deadline )); do
    if curl -fsS -o /dev/null "$url"; then
      green "  -> ${label} is up"
      return 0
    fi
    sleep 5
  done
  red "Timed out waiting for ${label} at ${url}"
  return 1
}

require() {
  command -v "$1" >/dev/null 2>&1 || { red "Missing required tool: $1"; exit 1; }
}

require docker
require curl

if ! docker compose version >/dev/null 2>&1; then
  red "docker compose plugin not found"
  exit 1
fi

info "Building Jenkins image (preinstalls Prometheus plugin)"
docker compose -f "$COMPOSE_FILE" build jenkins

info "Bringing up jenkins, prometheus, grafana"
docker compose -f "$COMPOSE_FILE" up -d jenkins prometheus grafana

wait_for_http "${JENKINS_URL}/login" "Jenkins login"
wait_for_http "${JENKINS_URL}/prometheus/" "Jenkins /prometheus endpoint"
wait_for_http "${PROM_URL}/-/ready" "Prometheus"
wait_for_http "${GRAFANA_URL}/api/health" "Grafana"

info "Inspecting Prometheus targets"
targets_json=$(curl -fsS "${PROM_URL}/api/v1/targets")
down_targets=$(printf '%s' "$targets_json" \
  | python -c "import sys,json; d=json.load(sys.stdin); print('\n'.join(t['labels'].get('job','?') for t in d['data']['activeTargets'] if t['health']!='up'))")

if [[ -n "$down_targets" ]]; then
  red "These scrape targets are not up:"
  red "$down_targets"
  red "Full target dump:"
  printf '%s\n' "$targets_json" | python -m json.tool >&2
  exit 1
fi

green "All Prometheus scrape targets are up."
green "Jenkins:    ${JENKINS_URL}"
green "Prometheus: ${PROM_URL}"
green "Grafana:    ${GRAFANA_URL}"
