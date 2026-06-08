variable "project" {
  type    = string
  default = "autostock"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

# Réseau
variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "availability_zones" {
  type    = list(string)
  default = ["us-east-1a", "us-east-1b"]
}

variable "bastion_allowed_cidrs" {
  type        = list(string)
  description = "Votre IP publique /32 (ex: [\"203.0.113.5/32\"])"
}

# Bastion
variable "ssh_public_key" {
  type        = string
  description = "Contenu de ~/.ssh/id_rsa.pub"
}

# Base de données
variable "db_name" {
  type    = string
  default = "autostockdb"
}

variable "db_username" {
  type    = string
  default = "autostock"
}

variable "db_password" {
  type      = string
  sensitive = true
}

variable "db_instance_class" {
  type    = string
  default = "db.t3.micro"
}

variable "db_allocated_storage" {
  type    = number
  default = 20
}

# Services AWS
variable "s3_bucket_name" {
  type    = string
  default = "autostock-docs-prod"
}

variable "ses_from_email" {
  type = string
}

variable "jwt_secret_value" {
  type      = string
  sensitive = true
}

variable "domain_name" {
  type    = string
  default = "autostock.example.com"
}

# ECS / Images
variable "image_tag" {
  type    = string
  default = "latest"
}

variable "ecs_backend_cpu" {
  type    = string
  default = "512"
}

variable "ecs_backend_memory" {
  type    = string
  default = "1024"
}

variable "ecs_backend_count" {
  type    = number
  default = 1
}

variable "ecs_frontend_count" {
  type    = number
  default = 1
}
