variable "project" {
  type    = string
  default = "autostock"
}

variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "s3_bucket_name" {
  type    = string
  default = "autostock-docs-prod"
}

variable "ses_from_email" {
  type        = string
  description = "Email vérifié dans SES (ex: noreply@votredomaine.com)"
}

variable "jwt_secret_value" {
  type      = string
  sensitive = true
  description = "Clé secrète JWT (même valeur que dans .env)"
}

variable "db_password" {
  type      = string
  sensitive = true
  description = "Mot de passe MySQL (pour Secrets Manager)"
}

variable "vps_ip" {
  type        = string
  description = "IP publique du VPS Hetzner (ex: 178.156.213.72)"
}

variable "domain_name" {
  type        = string
  default     = ""
  description = "Nom de domaine si vous en avez un (ex: autostock.com)"
}
