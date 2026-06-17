#!/bin/bash

if [ -f pom.xml ]; then
    echo "language=java" >> $GITHUB_OUTPUT

elif [ -f requirements.txt ]; then
    echo "language=python" >> $GITHUB_OUTPUT

elif [ -f go.mod ]; then
    echo "language=go" >> $GITHUB_OUTPUT

else
    echo "Unsupported project"
    exit 1
fi