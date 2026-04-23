# Prometheus monitoring

This is the Prometheus half of the DevSecOps stack. It scrapes three things
right now: Prometheus itself, the Jenkins server, and the Spring PetClinic
app. Grafana sits on top of Prometheus and reads from it; Grafana setup
lives under `../grafana/`.

## What's where

```
prometheus/
  prometheus.yml          main config (global, rule_files, scrape_configs)
  rules/
    jenkins.rules.yml     four alerts on the Jenkins job
    petclinic.rules.yml   three alerts on the PetClinic job
  targets/
    petclinic.json        file_sd target list for the app (Ansible templates this)
jenkins/
  Dockerfile              jenkins/jenkins:lts plus the prometheus plugin
  plugins.txt             plugin list installed at image build time
  casc.yaml               Configuration-as-Code: enables anonymous read,
                          configures the Prometheus endpoint at /prometheus/
scripts/
  bootstrap-prometheus.sh one-shot: build, up, wait, verify all targets up
```

## Bring it up

The fast path:

```bash
./scripts/bootstrap-prometheus.sh
```

That builds the Jenkins image (so the plugin is preinstalled), starts
Jenkins, Prometheus, and Grafana, waits for each to answer HTTP, then
checks `/api/v1/targets` and fails loudly if anything is down.

If you'd rather drive it manually:

```bash
docker compose -f docker-compose.devsecops.yml build jenkins
docker compose -f docker-compose.devsecops.yml up -d jenkins prometheus grafana
```

Then poke the URLs:

- Jenkins: http://localhost:8080
- Jenkins metrics: http://localhost:8080/prometheus/
- Prometheus: http://localhost:9090
- Prometheus targets: http://localhost:9090/targets
- Prometheus alerts: http://localhost:9090/alerts
- Grafana: http://localhost:3000 (admin / admin)

The Jenkins admin password defaults to `admin`. Set
`JENKINS_ADMIN_PASSWORD` in your environment before `docker compose up` if
you want something else. JCasC reads it at boot.

## How the Jenkins plugin gets installed

Vanilla `jenkins/jenkins:lts` does not ship the Prometheus metrics plugin,
so `/prometheus/` returns 404. The fix is the small image under
`jenkins/`:

1. `plugins.txt` lists every plugin the pipeline needs (prometheus, sonar,
   blueocean, JCasC, etc.).
2. The Dockerfile runs `jenkins-plugin-cli --plugin-file ...` during build,
   so plugins are baked in.
3. `casc.yaml` is copied to `/var/jenkins_conf/casc.yaml`. The
   `CASC_JENKINS_CONFIG` env var points Jenkins at it. JCasC applies it on
   first boot.
4. The setup wizard is skipped via `JAVA_OPTS=-Djenkins.install.runSetupWizard=false`
   so JCasC owns the config end to end.

The casc file does two things relevant to Prometheus. It allows anonymous
read on Jenkins (so the Prometheus container can scrape without an API
token), and it configures the Prometheus plugin endpoint at `/prometheus/`
with a 15s collection period.

If you need to change the path or lock the endpoint behind auth, edit
`jenkins/casc.yaml` and rebuild.

## How the app gets scraped

Spring Boot Actuator already ships in `pom.xml`. The missing piece was
`micrometer-registry-prometheus`. With it on the classpath, Spring Boot
auto-configures `/actuator/prometheus`. That endpoint is exposed in
`src/main/resources/application.properties`:

```
management.endpoints.web.exposure.include=health,info,prometheus,metrics
```

The scrape job in `prometheus.yml` uses `file_sd_configs` against
`prometheus/targets/petclinic.json`. That file is a placeholder with
`petclinic:8080` for local compose runs. When Ansible deploys the app to a
VM, the play should template the real host:port into that JSON. Prometheus
re-reads it every 30 seconds, no restart needed.

## Alerts

There are seven rules total. Severity is `warning` unless something is
fully unreachable, in which case it's `critical`.

Jenkins (`prometheus/rules/jenkins.rules.yml`):
- `JenkinsDown` — scrape failing for 2 minutes
- `JenkinsBuildQueueBacklog` — more than 5 jobs queued for 5 minutes
- `JenkinsExecutorStarvation` — over 90% executors busy for 10 minutes
- `JenkinsBuildFailureRateHigh` — recent failure ratio is high

PetClinic (`prometheus/rules/petclinic.rules.yml`):
- `PetclinicDown` — scrape failing for 2 minutes
- `PetclinicHighErrorRate` — 5xx rate above 5% for 5 minutes
- `PetclinicHighLatencyP95` — p95 latency above 1 second for 10 minutes

The rules are loaded but no Alertmanager is wired in. They show up under
`/alerts` in the Prometheus UI. Adding Alertmanager is out of scope for
this slice; if someone picks it up, set `alerting:` in `prometheus.yml`
and add a service in `docker-compose.devsecops.yml`.

## Verifying

After `up -d`, the cheap sanity checks:

```bash
curl -fs http://localhost:9090/-/ready                 # Prometheus is up
curl -fs http://localhost:8080/prometheus/ | head      # Jenkins metrics
curl -fs http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | {job: .labels.job, health}'
```

The third one should print `prometheus`, `jenkins`, and `petclinic`, all
with `health: "up"`. If `petclinic` is down and the app is not deployed
yet, that's expected; update `prometheus/targets/petclinic.json` once the
host exists.

A few PromQL queries that are useful for the demo:

```
up                                                 # which targets are reachable
rate(http_server_requests_seconds_count{job="petclinic"}[1m])
sum by (status) (rate(http_server_requests_seconds_count{job="petclinic"}[5m]))
default_jenkins_builds_last_build_result_ordinal
```

## Troubleshooting

`/prometheus/` on Jenkins returns 404. The plugin didn't install or you're
hitting a stale image. Rebuild with `docker compose build --no-cache jenkins`
and recreate the container. Check the Jenkins log for `Loaded plugin
'prometheus'`.

`/prometheus/` on Jenkins returns 403. Anonymous read is off in
`casc.yaml`. Either turn it back on, or scrape with an API token (set
`Authorization` header in a custom scrape config).

PetClinic target shows `down` with `connection refused`. The host in
`prometheus/targets/petclinic.json` is wrong, or the app isn't running on
that host. Check from inside the prometheus container:
`docker exec prometheus wget -qO- http://<host>:8080/actuator/prometheus`.

`/actuator/prometheus` returns 404 on the app. The micrometer registry is
missing from `pom.xml` or the build is stale. Rebuild the JAR.

A rule won't load. Run `docker exec prometheus promtool check rules
/etc/prometheus/rules/*.yml` for the line number.

## Coordination notes

The Jenkins service in `docker-compose.devsecops.yml` switched from
`image:` to `build: ./jenkins`. Volume names, ports, and the network are
unchanged, so existing Jenkins state in the `jenkins-data` volume carries
over. If anyone ran the old image first, the casc file will still apply
because `CASC_JENKINS_CONFIG` lives outside `JENKINS_HOME`.

Grafana already points at `http://prometheus:9090` (Jerome's work). New
PromQL queries land cleanly in any dashboard he adds.
