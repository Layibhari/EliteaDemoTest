#!/usr/bin/env bash
# Test: Built CSS has Montserrat and Varela Round font-face declarations
set -euo pipefail
cd "$(dirname "$0")"

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: Fonts in CSS output ---"

bun run build 2>&1 | tail -1

CSS_FILE=$(ls ../../../target/classes/static/assets/*.css 2>/dev/null | head -1)
if [ -z "$CSS_FILE" ]; then
  echo "FAIL: no CSS file found"
  exit 1
fi

FAILURES=0

# Check for Montserrat font-face
if ! grep -q 'Montserrat' "$CSS_FILE"; then
  echo "FAIL: Montserrat font not found in CSS"
  FAILURES=$((FAILURES + 1))
fi

# Check for Varela Round font-face
if ! grep -q 'Varela Round' "$CSS_FILE"; then
  echo "FAIL: Varela Round font not found in CSS"
  FAILURES=$((FAILURES + 1))
fi

# Check font files are referenced
if ! grep -q 'montserrat-webfont' "$CSS_FILE"; then
  echo "FAIL: montserrat-webfont not referenced in CSS"
  FAILURES=$((FAILURES + 1))
fi

if ! grep -q 'varela_round-webfont' "$CSS_FILE"; then
  echo "FAIL: varela_round-webfont not referenced in CSS"
  FAILURES=$((FAILURES + 1))
fi

if [ "$FAILURES" -gt 0 ]; then
  echo "FAIL: $FAILURES assertion(s) failed"
  exit 1
fi

echo "PASS: Montserrat and Varela Round fonts configured"
