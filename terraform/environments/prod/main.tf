# ============================================================
# Environnement PROD : AWS réel
# Infra complète : VPC, Bastion, RDS, ECR, ECS Fargate, ALB
# ============================================================

terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Remote state S3 (décommenter après création du bucket terraform-state)
  # backend "s3" {
  #   bucket  = "autostock-terraform-state"
  #   key     = "prod/terraform.tfstate"
  #   region  = "us-east-1"
  #   encrypt = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.tags
  }
}

locals {
  tags = {
    Project     = var.project
    Environment = "prod"
    ManagedBy   = "terraform"
  }
}

# ---- Réseau ----
module "networking" {
  source = "../../modules/networking"

  project               = var.project
  vpc_cidr              = var.vpc_cidr
  availability_zones    = var.availability_zones
  bastion_allowed_cidrs = var.bastion_allowed_cidrs
  tags                  = local.tags
}

# ---- Bastion SSH ----
module "bastion" {
  source = "../../modules/bastion"

  project          = var.project
  public_subnet_id = module.networking.public_subnet_ids[0]
  sg_bastion_id    = module.networking.sg_bastion_id
  ssh_public_key   = var.ssh_public_key
  tags             = local.tags
}

# ---- RDS MySQL ----
module "database" {
  source = "../../modules/database"

  project             = var.project
  private_subnet_ids  = module.networking.private_subnet_ids
  sg_rds_id           = module.networking.sg_rds_id
  db_name             = var.db_name
  db_username         = var.db_username
  db_password         = var.db_password
  instance_class      = var.db_instance_class
  allocated_storage   = var.db_allocated_storage
  deletion_protection = true
  tags                = local.tags
}

# ---- Services AWS (S3, SES, SQS, Secrets Manager) ----
module "aws_services" {
  source = "../../modules/aws-services"

  project              = var.project
  environment          = "prod"
  s3_bucket_name       = var.s3_bucket_name
  ses_from_email       = var.ses_from_email
  cors_allowed_origins = ["https://${var.domain_name}"]
  jwt_secret_value     = var.jwt_secret_value
  db_password_value    = var.db_password
  tags                 = local.tags
}

# ---- ECR (registres Docker) ----
module "ecr" {
  source  = "../../modules/ecr"
  project = var.project
  tags    = local.tags
}

# ---- ALB ----
module "alb" {
  source = "../../modules/alb"

  project           = var.project
  environment       = "prod"
  vpc_id            = module.networking.vpc_id
  public_subnet_ids = module.networking.public_subnet_ids
  sg_alb_id         = module.networking.sg_alb_id
  tags              = local.tags
}

# ---- ECS Fargate ----
module "ecs" {
  source = "../../modules/ecs"

  project               = var.project
  aws_region            = var.aws_region
  private_subnet_ids    = module.networking.private_subnet_ids
  sg_ecs_backend_id     = module.networking.sg_ecs_backend_id
  sg_ecs_frontend_id    = module.networking.sg_ecs_frontend_id
  backend_tg_arn        = module.alb.backend_tg_arn
  frontend_tg_arn       = module.alb.frontend_tg_arn
  alb_http_listener_arn = module.alb.http_listener_arn

  backend_image  = module.ecr.backend_repo_url
  frontend_image = module.ecr.frontend_repo_url
  image_tag      = var.image_tag

  backend_cpu            = var.ecs_backend_cpu
  backend_memory         = var.ecs_backend_memory
  backend_desired_count  = var.ecs_backend_count
  frontend_desired_count = var.ecs_frontend_count

  db_endpoint            = module.database.address
  db_name                = var.db_name
  db_username            = var.db_username
  db_password_secret_arn = module.aws_services.db_password_secret_arn
  jwt_secret_arn         = module.aws_services.jwt_secret_arn

  s3_bucket_name = module.aws_services.s3_bucket_name
  ses_from_email = var.ses_from_email
  sqs_queue_url  = module.aws_services.sqs_queue_url
  sqs_queue_arn  = module.aws_services.sqs_queue_arn

  tags = local.tags
}
