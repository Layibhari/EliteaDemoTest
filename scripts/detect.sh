#!/bin/bash

set -e

if [ -f pom.xml ]; then
    echo "language=java" >> "$GITHUB_OUTPUT"

elif [ -f requirements.txt ] || [ -f pyproject.toml ]; then
    echo "language=python" >> "$GITHUB_OUTPUT"

elif [ -f go.mod ]; then
    echo "language=go" >> "$GITHUB_OUTPUT"

else
    echo "Unsupported project type"
    exit 1
fi