variable "project"               { type = string }
variable "aws_region"            { type = string }
variable "private_subnet_ids"    { type = list(string) }
variable "sg_ecs_backend_id"     { type = string }
variable "sg_ecs_frontend_id"    { type = string }
variable "backend_tg_arn"        { type = string }
variable "frontend_tg_arn"       { type = string }
variable "alb_http_listener_arn" { type = string }

variable "backend_image"  { type = string }
variable "frontend_image" { type = string }

variable "image_tag" {
  type    = string
  default = "latest"
}

variable "backend_cpu" {
  type    = string
  default = "512"
}

variable "backend_memory" {
  type    = string
  default = "1024"
}

variable "backend_desired_count" {
  type    = number
  default = 1
}

variable "frontend_desired_count" {
  type    = number
  default = 1
}

variable "db_endpoint"             { type = string }
variable "db_name"                 { type = string }
variable "db_username"             { type = string }
variable "db_password_secret_arn"  { type = string }
variable "jwt_secret_arn"          { type = string }
variable "s3_bucket_name"          { type = string }
variable "ses_from_email"          { type = string }
variable "sqs_queue_url"           { type = string }
variable "sqs_queue_arn"           { type = string }

variable "tags" {
  type    = map(string)
  default = {}
}
