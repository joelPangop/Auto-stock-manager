# ============================================================
# Environnement DEV : LocalStack
# Seuls les services AWS sont provisionnés ici.
# Le réseau, ECS et RDS tournent dans Docker Compose.
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
  region                      = var.aws_region
  access_key                  = "test"
  secret_key                  = "test"
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    s3             = "http://localhost:4566"
    ses            = "http://localhost:4566"
    sqs            = "http://localhost:4566"
    secretsmanager = "http://localhost:4566"
  }

  # S3 path-style requis pour LocalStack
  s3_use_path_style = true
}

module "aws_services" {
  source = "../../modules/aws-services"

  project     = var.project
  environment = "dev"

  s3_bucket_name       = var.s3_bucket_name
  ses_from_email       = var.ses_from_email
  cors_allowed_origins = ["http://localhost:4200"]

  jwt_secret_value  = var.jwt_secret_value
  db_password_value = var.db_password_value

  tags = local.tags
}

locals {
  tags = {
    Project     = var.project
    Environment = "dev"
    ManagedBy   = "terraform"
  }
}
