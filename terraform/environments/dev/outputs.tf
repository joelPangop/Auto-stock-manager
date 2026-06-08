output "s3_bucket" { value = module.aws_services.s3_bucket_name }
output "sqs_url" { value = module.aws_services.sqs_queue_url }
output "sqs_dlq_url" { value = module.aws_services.sqs_dlq_url }
