#!/usr/bin/env bash
# Test: Built JS contains svelte-spa-router with hash route configuration
set -euo pipefail
cd "$(dirname "$0")"

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: Hash router configured ---"

bun run build 2>&1 | tail -1

JS_FILE=$(ls ../../../target/classes/static/assets/*.js 2>/dev/null | head -1)
if [ -z "$JS_FILE" ]; then
  echo "FAIL: no JS bundle found"
  exit 1
fi

FAILURES=0

# Check for svelte-spa-router hash routing
if ! grep -q 'hashchange' "$JS_FILE"; then
  echo "FAIL: hashchange listener not found (no hash router)"
  FAILURES=$((FAILURES + 1))
fi

# Check for route configuration (welcome route)
if ! grep -q '/welcome' "$JS_FILE"; then
  echo "FAIL: /welcome route not found in bundle"
  FAILURES=$((FAILURES + 1))
fi

# Check that router renders into Layout (nav links use /#/ prefix)
if ! grep -q '/#/' "$JS_FILE"; then
  echo "FAIL: hash-based route links (/#/) not found"
  FAILURES=$((FAILURES + 1))
fi

if [ "$FAILURES" -gt 0 ]; then
  echo "FAIL: $FAILURES assertion(s) failed"
  exit 1
fi

echo "PASS: Hash router configured with welcome route"
