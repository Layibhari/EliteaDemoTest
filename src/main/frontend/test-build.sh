#!/usr/bin/env bash
# Test: bun run build produces output in target/classes/static/
set -euo pipefail
cd "$(dirname "$0")"

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: bun run build produces static output ---"

# Run build
bun run build
BUILD_EXIT=$?

if [ $BUILD_EXIT -ne 0 ]; then
  echo "FAIL: bun run build exited with $BUILD_EXIT"
  exit 1
fi

# Assert output exists
OUTPUT_DIR="../../../target/classes/static"
if [ ! -d "$OUTPUT_DIR" ]; then
  echo "FAIL: output directory $OUTPUT_DIR does not exist"
  exit 1
fi

# Assert at least one .js file exists
JS_COUNT=$(find "$OUTPUT_DIR" -name "*.js" 2>/dev/null | wc -l)
if [ "$JS_COUNT" -eq 0 ]; then
  echo "FAIL: no .js files found in $OUTPUT_DIR"
  exit 1
fi

echo "PASS: build produced $JS_COUNT JS file(s) in $OUTPUT_DIR"
