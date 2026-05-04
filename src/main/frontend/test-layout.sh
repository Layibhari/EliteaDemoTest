#!/usr/bin/env bash
# Test: Built JS contains navbar with nav links and footer
set -euo pipefail
cd "$(dirname "$0")"

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: Layout renders navbar, links, footer ---"

bun run build 2>&1 | tail -3
BUILD_EXIT=${PIPESTATUS[0]}

if [ $BUILD_EXIT -ne 0 ]; then
  echo "FAIL: build failed"
  exit 1
fi

# Find built JS file
JS_FILE=$(ls ../../../target/classes/static/assets/*.js 2>/dev/null | head -1)
if [ -z "$JS_FILE" ]; then
  echo "FAIL: no JS bundle found"
  exit 1
fi

FAILURES=0

# Assert Home link
if ! grep -q 'Home' "$JS_FILE"; then
  echo "FAIL: 'Home' nav link not found in bundle"
  FAILURES=$((FAILURES + 1))
fi

# Assert Find Owners link
if ! grep -q 'Find Owners' "$JS_FILE"; then
  echo "FAIL: 'Find Owners' nav link not found in bundle"
  FAILURES=$((FAILURES + 1))
fi

# Assert Veterinarians link
if ! grep -q 'Veterinarians' "$JS_FILE"; then
  echo "FAIL: 'Veterinarians' nav link not found in bundle"
  FAILURES=$((FAILURES + 1))
fi

# Assert Error link
if ! grep -q 'Error' "$JS_FILE"; then
  echo "FAIL: 'Error' nav link not found in bundle"
  FAILURES=$((FAILURES + 1))
fi

# Assert footer with spring logo
if ! grep -q 'spring-logo.svg' "$JS_FILE"; then
  echo "FAIL: footer spring logo not found in bundle"
  FAILURES=$((FAILURES + 1))
fi

if [ "$FAILURES" -gt 0 ]; then
  echo "FAIL: $FAILURES assertion(s) failed"
  exit 1
fi

echo "PASS: Layout has navbar with 4 links and footer"
