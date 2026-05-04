#!/usr/bin/env bash
# Test: Built JS contains Lucide SVG icons
set -euo pipefail
cd "$(dirname "$0")"

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: Lucide icons in nav links ---"

bun run build 2>&1 | tail -1

JS_FILE=$(ls ../../../target/classes/static/assets/*.js 2>/dev/null | head -1)
if [ -z "$JS_FILE" ]; then
  echo "FAIL: no JS bundle found"
  exit 1
fi

FAILURES=0

# Check for Lucide SVG namespace (present in all Lucide icons)
if ! grep -q 'w3.org/2000/svg' "$JS_FILE"; then
  echo "FAIL: no SVG namespace reference found"
  FAILURES=$((FAILURES + 1))
fi

# Check for Lucide CSS class pattern
if ! grep -q 'lucide-icon' "$JS_FILE"; then
  echo "FAIL: no lucide-icon class found"
  FAILURES=$((FAILURES + 1))
fi

# Check for specific icon SVG path data (house icon distinctive path)
if ! grep -q 'M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8' "$JS_FILE"; then
  echo "FAIL: house icon path not found"
  FAILURES=$((FAILURES + 1))
fi

# Check for stethoscope icon (distinctive path)
if ! grep -q 'M11 2v2' "$JS_FILE"; then
  echo "FAIL: stethoscope icon path not found"
  FAILURES=$((FAILURES + 1))
fi

if [ "$FAILURES" -gt 0 ]; then
  echo "FAIL: $FAILURES assertion(s) failed"
  exit 1
fi

echo "PASS: Lucide icons present in bundle"
