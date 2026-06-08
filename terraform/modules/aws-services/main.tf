# ============================================================
# Module : AWS Services (S3, SES, SQS, Secrets Manager)
# Compatible LocalStack (dev) et AWS réel (prod)
# ============================================================

# ---- S3 Bucket ----
resource "aws_s3_bucket" "docs" {
  bucket        = var.s3_bucket_name
  force_destroy = var.environment == "dev"

  tags = merge(var.tags, { Name = var.s3_bucket_name })
}

resource "aws_s3_bucket_versioning" "docs" {
  bucket = aws_s3_bucket.docs.id
  versioning_configuration {
    status = var.environment == "prod" ? "Enabled" : "Disabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "docs" {
  bucket = aws_s3_bucket.docs.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

resource "aws_s3_bucket_cors_configuration" "docs" {
  bucket = aws_s3_bucket.docs.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET", "PUT", "POST", "DELETE"]
    allowed_origins = var.cors_allowed_origins
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

# Bloquer l'accès public (fichiers accessibles uniquement via pré-signé ou backend)
resource "aws_s3_bucket_public_access_block" "docs" {
  bucket                  = aws_s3_bucket.docs.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# ---- SES : vérification du domaine/email expéditeur ----
resource "aws_ses_email_identity" "sender" {
  email = var.ses_from_email
}

# ---- SQS Queue ----
resource "aws_sqs_queue" "notifications" {
  name                      = "${var.project}-notifications"
  message_retention_seconds = 86400   # 1 jour
  visibility_timeout_seconds = 30

  tags = merge(var.tags, { Name = "${var.project}-notifications" })
}

# Dead-letter queue
resource "aws_sqs_queue" "notifications_dlq" {
  name                      = "${var.project}-notifications-dlq"
  message_retention_seconds = 1209600 # 14 jours

  tags = merge(var.tags, { Name = "${var.project}-notifications-dlq" })
}

resource "aws_sqs_queue_redrive_policy" "notifications" {
  queue_url = aws_sqs_queue.notifications.id
  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notifications_dlq.arn
    maxReceiveCount     = 3
  })
}

# ---- Secrets Manager ----
resource "aws_secretsmanager_secret" "jwt_secret" {
  name                    = "${var.project}/jwt-secret"
  description             = "Clé secrète JWT pour ${var.project}"
  recovery_window_in_days = var.environment == "prod" ? 7 : 0

  tags = var.tags
}

resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id     = aws_secretsmanager_secret.jwt_secret.id
  secret_string = var.jwt_secret_value
}

resource "aws_secretsmanager_secret" "db_password" {
  name                    = "${var.project}/db-password"
  description             = "Mot de passe RDS MySQL pour ${var.project}"
  recovery_window_in_days = var.environment == "prod" ? 7 : 0

  tags = var.tags
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password_value
}
