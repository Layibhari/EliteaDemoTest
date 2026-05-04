#!/usr/bin/env bash
# Test: Spring Boot serves Svelte index.html as static resource
set -euo pipefail
cd "$(dirname "$0")/../../.."

export PATH="$HOME/.bun/bin:$PATH"

echo "--- Test: Spring Boot serves Svelte index.html ---"

# Build first to ensure static assets exist
./mvnw compile -DskipTests -q 2>&1

PORT=8099

# Start Spring Boot in background
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT" -q > /tmp/sb-out.log 2>&1 &
SB_PID=$!

# Wait for startup
echo "Waiting for Spring Boot to start on port $PORT..."
for i in $(seq 1 60); do
  if curl -s "http://localhost:$PORT/" > /dev/null 2>&1; then
    break
  fi
  sleep 1
  if [ $i -eq 60 ]; then
    echo "FAIL: Spring Boot did not start within 60s"
    kill $SB_PID 2>/dev/null || true
    exit 1
  fi
done

# Fetch /index.html (static resource, not intercepted by WelcomeController)
RESPONSE=$(curl -s "http://localhost:$PORT/index.html")

# Kill Spring Boot
kill $SB_PID 2>/dev/null || true
wait $SB_PID 2>/dev/null || true

# Assert Svelte app markers
if echo "$RESPONSE" | grep -q '<div id="app">'; then
  echo "PASS: Spring Boot serves Svelte index.html at /index.html"
else
  echo "FAIL: /index.html does not contain Svelte app mount point"
  echo "Got: $RESPONSE"
  exit 1
fi
