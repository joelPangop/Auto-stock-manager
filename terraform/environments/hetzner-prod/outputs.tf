output "s3_bucket_name" {
  value = module.aws_services.s3_bucket_name
}

output "sqs_queue_url" {
  value = module.aws_services.sqs_queue_url
}

output "iam_access_key_id" {
  description = "AWS_ACCESS_KEY_ID à mettre dans le .env du VPS"
  value       = aws_iam_access_key.vps.id
}

output "iam_secret_access_key" {
  description = "AWS_SECRET_ACCESS_KEY à mettre dans le .env du VPS"
  value       = aws_iam_access_key.vps.secret
  sensitive   = true
}
