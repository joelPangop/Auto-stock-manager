# ============================================================
# Environnement Hetzner-Prod
# Crée uniquement les services AWS nécessaires depuis un VPS externe :
# S3, SES, SQS, Secrets Manager + IAM user avec accès limité
# ============================================================

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

locals {
  tags = {
    Project     = var.project
    Environment = "prod"
    ManagedBy   = "terraform"
  }
}

# ---- S3, SES, SQS, Secrets Manager ----
module "aws_services" {
  source = "../../modules/aws-services"

  project              = var.project
  environment          = "prod"
  s3_bucket_name       = var.s3_bucket_name
  ses_from_email       = var.ses_from_email
  cors_allowed_origins = ["http://${var.vps_ip}", "https://${var.domain_name}"]
  jwt_secret_value     = var.jwt_secret_value
  db_password_value    = var.db_password
  tags                 = local.tags
}

# ---- IAM User pour le VPS Hetzner ----
resource "aws_iam_user" "vps" {
  name = "${var.project}-vps-user"
  tags = local.tags
}

resource "aws_iam_access_key" "vps" {
  user = aws_iam_user.vps.name
}

resource "aws_iam_user_policy" "vps" {
  name = "${var.project}-vps-policy"
  user = aws_iam_user.vps.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "S3Access"
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Resource = [
          "arn:aws:s3:::${var.s3_bucket_name}",
          "arn:aws:s3:::${var.s3_bucket_name}/*"
        ]
      },
      {
        Sid    = "SESAccess"
        Effect = "Allow"
        Action = [
          "ses:SendEmail",
          "ses:SendRawEmail"
        ]
        Resource = "*"
      },
      {
        Sid    = "SQSAccess"
        Effect = "Allow"
        Action = [
          "sqs:SendMessage",
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes",
          "sqs:GetQueueUrl"
        ]
        Resource = module.aws_services.sqs_queue_arn
      }
    ]
  })
}
