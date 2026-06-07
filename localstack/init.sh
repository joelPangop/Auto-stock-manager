#!/bin/bash
# =============================================================================
# LocalStack init script — exécuté automatiquement au démarrage
# Crée les ressources AWS simulées pour AutoStock
# =============================================================================

set -e

ENDPOINT="http://localhost:4566"
REGION="us-east-1"
AWS="aws --endpoint-url=$ENDPOINT --region=$REGION"

echo "=========================================="
echo "  AutoStock — Initialisation LocalStack"
echo "=========================================="

# -----------------------------------------------------------------------------
# S3 — Bucket pour les documents et photos de voitures
# -----------------------------------------------------------------------------
echo "[S3] Création du bucket autostock-docs..."
$AWS s3api create-bucket --bucket autostock-docs 2>/dev/null || echo "[S3] Bucket déjà existant"

$AWS s3api put-bucket-cors --bucket autostock-docs --cors-configuration '{
  "CORSRules": [{
    "AllowedOrigins": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedHeaders": ["*"],
    "MaxAgeSeconds": 3000
  }]
}'
echo "[S3] Bucket autostock-docs prêt ✓"

# -----------------------------------------------------------------------------
# SES — Vérification de l'adresse email expéditrice
# -----------------------------------------------------------------------------
echo "[SES] Vérification de l'email expéditeur..."
$AWS ses verify-email-identity --email-address joelpangop@gmail.com 2>/dev/null || true
echo "[SES] Email joelpangop@gmail.com vérifié ✓"

# -----------------------------------------------------------------------------
# SQS — File de notifications async
# -----------------------------------------------------------------------------
echo "[SQS] Création de la queue autostock-notifications..."
$AWS sqs create-queue --queue-name autostock-notifications \
  --attributes '{
    "MessageRetentionPeriod": "86400",
    "VisibilityTimeout": "30"
  }' 2>/dev/null || echo "[SQS] Queue déjà existante"
echo "[SQS] Queue autostock-notifications prête ✓"

# -----------------------------------------------------------------------------
# Secrets Manager — Secrets de l'application
# -----------------------------------------------------------------------------
echo "[SecretsManager] Création des secrets..."

$AWS secretsmanager create-secret \
  --name "autostock/jwt-secret" \
  --secret-string "VGhpcy1pcy1hLXN0cm9uZy1iYXNlNjQtc2VjcmV0LWtleS1jaGFuZ2UtbWUtIQpCMV9OU0RKS1BfQzRzM2JxQ1Q=" \
  2>/dev/null || echo "[SecretsManager] Secret jwt-secret déjà existant"

$AWS secretsmanager create-secret \
  --name "autostock/db-password" \
  --secret-string "Abc123..." \
  2>/dev/null || echo "[SecretsManager] Secret db-password déjà existant"

echo "[SecretsManager] Secrets créés ✓"

echo "=========================================="
echo "  Initialisation terminée avec succès !"
echo "=========================================="
