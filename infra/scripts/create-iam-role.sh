#!/bin/bash

# ============================================================
# Script: create-iam-role.sh
# Purpose: Create IAM role for External Secrets to access
#          AWS Secrets Manager
# Usage: bash infra/scripts/create-iam-role.sh
# ============================================================

set -e

AWS_REGION="us-east-1"
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
CLUSTER_NAME="petclinic-cluster"
ROLE_NAME="petclinic-external-secrets-role"
POLICY_NAME="petclinic-secrets-manager-policy"
NAMESPACE="external-secrets"
SERVICE_ACCOUNT="external-secrets"

echo "Account ID: $ACCOUNT_ID"
echo "Cluster: $CLUSTER_NAME"

# Step 1 — Get OIDC provider
echo ""
echo "Step 1 — Getting OIDC provider..."
OIDC_URL=$(aws eks describe-cluster \
  --name $CLUSTER_NAME \
  --query "cluster.identity.oidc.issuer" \
  --output text)
OIDC_ID=$(echo $OIDC_URL | cut -d'/' -f5)
echo "OIDC ID: $OIDC_ID"

# Step 2 — Create IAM policy using inline JSON
echo ""
echo "Step 2 — Creating IAM policy..."
POLICY_ARN=$(aws iam create-policy \
  --policy-name $POLICY_NAME \
  --policy-document "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"secretsmanager:GetSecretValue\",\"secretsmanager:DescribeSecret\"],\"Resource\":\"arn:aws:secretsmanager:${AWS_REGION}:${ACCOUNT_ID}:secret:petclinic/*\"}]}" \
  --query "Policy.Arn" \
  --output text)
echo "Policy ARN: $POLICY_ARN"

# Step 3 — Create IAM role using inline trust policy
echo ""
echo "Step 3 — Creating IAM role with trust policy..."
ROLE_ARN=$(aws iam create-role \
  --role-name $ROLE_NAME \
  --assume-role-policy-document "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"Federated\":\"arn:aws:iam::${ACCOUNT_ID}:oidc-provider/oidc.eks.${AWS_REGION}.amazonaws.com/id/${OIDC_ID}\"},\"Action\":\"sts:AssumeRoleWithWebIdentity\",\"Condition\":{\"StringEquals\":{\"oidc.eks.${AWS_REGION}.amazonaws.com/id/${OIDC_ID}:sub\":\"system:serviceaccount:${NAMESPACE}:${SERVICE_ACCOUNT}\"}}}]}" \
  --query "Role.Arn" \
  --output text)
echo "Role ARN: $ROLE_ARN"

# Step 4 — Attach policy to role
echo ""
echo "Step 4 — Attaching policy to role..."
aws iam attach-role-policy \
  --role-name $ROLE_NAME \
  --policy-arn $POLICY_ARN

echo ""
echo "✅ IAM role created successfully!"
echo "Role ARN: $ROLE_ARN"
echo ""
echo "Save this Role ARN — needed for next step!"
