#!/bin/bash

# ============================================================
# Script: install-external-secrets.sh
# Purpose: Install External Secrets Operator in EKS
# Usage: bash infra/scripts/install-external-secrets.sh
# ============================================================

set -e

echo "Step 1 — Adding External Secrets Helm repo..."
helm repo add external-secrets https://charts.external-secrets.io
helm repo update

echo ""
echo "Step 2 — Installing External Secrets Operator..."
helm install external-secrets external-secrets/external-secrets \
  --namespace external-secrets \
  --create-namespace \
  --timeout 5m

echo ""
echo "Step 3 — Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod \
  --all -n external-secrets \
  --timeout=120s

echo ""
echo "Step 4 — Verifying pods..."
kubectl get pods -n external-secrets

echo ""
echo "✅ External Secrets Operator installed successfully!"
