#!/usr/bin/env bash
# Test: Built JS contains InputField and SelectField form components
set -euo pipefail
cd "$(dirname "$0")"

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: Form components ---"

bun run build 2>&1 | tail -1
BUILD_EXIT=${PIPESTATUS[0]}

if [ $BUILD_EXIT -ne 0 ]; then
  echo "FAIL: build failed"
  exit 1
fi

JS_FILE=$(ls ../../../target/classes/static/assets/*.js 2>/dev/null | head -1)
if [ -z "$JS_FILE" ]; then
  echo "FAIL: no JS bundle found"
  exit 1
fi

FAILURES=0

# InputField: wrapper uses form-group class
if ! grep -q 'form-group' "$JS_FILE"; then
  echo "FAIL: form-group class not found (InputField/SelectField wrapper)"
  FAILURES=$((FAILURES + 1))
fi

# InputField: label uses control-label class
if ! grep -q 'control-label' "$JS_FILE"; then
  echo "FAIL: control-label class not found (field label)"
  FAILURES=$((FAILURES + 1))
fi

# InputField: error message uses help-inline class
if ! grep -q 'help-inline' "$JS_FILE"; then
  echo "FAIL: help-inline class not found (inline error message)"
  FAILURES=$((FAILURES + 1))
fi

# InputField: supports type="text"
if ! grep -q 'type:`text`' "$JS_FILE"; then
  echo "FAIL: type text not found (InputField text support)"
  FAILURES=$((FAILURES + 1))
fi

# InputField: supports type="date"
if ! grep -q 'type:`date`' "$JS_FILE"; then
  echo "FAIL: type date not found (InputField date support)"
  FAILURES=$((FAILURES + 1))
fi

# SelectField: renders select element with options
if ! grep -q 'select' "$JS_FILE"; then
  echo "FAIL: select element not found (SelectField)"
  FAILURES=$((FAILURES + 1))
fi

# SelectField: renders option elements
if ! grep -q '<option' "$JS_FILE"; then
  echo "FAIL: option element not found (SelectField options)"
  FAILURES=$((FAILURES + 1))
fi

# felte-compatible: touched && error validation pattern
if ! grep -q 'touched' "$JS_FILE"; then
  echo "FAIL: touched prop not found (felte touched state)"
  FAILURES=$((FAILURES + 1))
fi

if ! grep -q 'props' "$JS_FILE"; then
  echo "FAIL: props not found (Svelte 5 props)"
  FAILURES=$((FAILURES + 1))
fi

# Input has form-control matching Tailwind classes
if ! grep -q 'w-full' "$JS_FILE"; then
  echo "FAIL: w-full not found (full-width input)"
  FAILURES=$((FAILURES + 1))
fi

if [ "$FAILURES" -gt 0 ]; then
  echo "FAIL: $FAILURES assertion(s) failed"
  exit 1
fi

echo "PASS: InputField and SelectField form components present"
