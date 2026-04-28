#!/bin/bash

# ============================================================
# Script: create-secrets.sh
# Purpose: Store PetClinic secrets in AWS Secrets Manager
# Usage: bash infra/scripts/create-secrets.sh
# ============================================================

set -e   # exit on any error

AWS_REGION="us-east-1"
SECRET_NAME="petclinic/db-credentials"

echo "Creating secret: $SECRET_NAME ..."

aws secretsmanager create-secret \
  --region $AWS_REGION \
  --name $SECRET_NAME \
  --secret-string '{
    "username": "admin",
    "password": "password123",
    "host": "terraform-20260424215710591600000001.cyrwms44o6sg.us-east-1.rds.amazonaws.com",
    "port": "3306",
    "database": "petclinic"
  }'

echo "✅ Secret created successfully!"
echo ""
echo "Verifying..."
aws secretsmanager list-secrets \
  --query "SecretList[*].Name" \
  --output table
