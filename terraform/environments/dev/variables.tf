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
  default = "autostock-docs"
}

variable "ses_from_email" {
  type = string
}

variable "jwt_secret_value" {
  type      = string
  sensitive = true
}

variable "db_password_value" {
  type      = string
  sensitive = true
}
