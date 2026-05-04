#!/usr/bin/env bash
# Test: bun run check fails on malformed code
set -euo pipefail
cd "$(dirname "$0")"

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: bun run check catches errors ---"

# Create a temp file with bad formatting
BAD_FILE="src/test-bad-format.ts"
echo "const   x   =   1;" > "$BAD_FILE"

# Run check - should fail
set +e
bun run check 2>/dev/null
CHECK_EXIT=$?
set -e

# Clean up
rm -f "$BAD_FILE"

if [ "$CHECK_EXIT" -ne 0 ]; then
  echo "PASS: bun run check exited with $CHECK_EXIT (caught bad formatting)"
else
  echo "FAIL: bun run check exited 0 but should have caught formatting errors"
  exit 1
fi
