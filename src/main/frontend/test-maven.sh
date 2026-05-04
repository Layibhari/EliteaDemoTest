#!/usr/bin/env bash
# Test: ./mvnw compile includes frontend build in target/classes/static/
set -euo pipefail
cd "$(dirname "$0")/../../.."

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: ./mvnw compile produces frontend output ---"

# Clean previous output
rm -rf target/classes/static/

# Run maven compile (skip tests for speed)
./mvnw compile -DskipTests -q 2>&1
MAVEN_EXIT=$?

if [ $MAVEN_EXIT -ne 0 ]; then
  echo "FAIL: ./mvnw compile exited with $MAVEN_EXIT"
  exit 1
fi

# Assert output exists
if [ ! -d "target/classes/static" ]; then
  echo "FAIL: target/classes/static does not exist after compile"
  exit 1
fi

# Assert at least one .js file exists
JS_COUNT=$(find "target/classes/static" -name "*.js" 2>/dev/null | wc -l)
if [ "$JS_COUNT" -eq 0 ]; then
  echo "FAIL: no .js files in target/classes/static after compile"
  exit 1
fi

echo "PASS: ./mvnw compile produced frontend output ($JS_COUNT JS files)"
