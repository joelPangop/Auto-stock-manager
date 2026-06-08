variable "project" {
  type = string
}

variable "environment" {
  type = string
}

variable "s3_bucket_name" {
  type = string
}

variable "ses_from_email" {
  type = string
}

variable "cors_allowed_origins" {
  type    = list(string)
  default = ["*"]
}

variable "jwt_secret_value" {
  type      = string
  sensitive = true
}

variable "db_password_value" {
  type      = string
  sensitive = true
}

variable "tags" {
  type    = map(string)
  default = {}
}
