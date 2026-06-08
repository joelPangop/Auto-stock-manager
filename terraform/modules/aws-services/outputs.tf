output "s3_bucket_name"       { value = aws_s3_bucket.docs.bucket }
output "s3_bucket_arn"        { value = aws_s3_bucket.docs.arn }
output "sqs_queue_url"        { value = aws_sqs_queue.notifications.url }
output "sqs_queue_arn"        { value = aws_sqs_queue.notifications.arn }
output "sqs_dlq_url"          { value = aws_sqs_queue.notifications_dlq.url }
output "ses_identity_arn"     { value = aws_ses_email_identity.sender.arn }
output "jwt_secret_arn"       { value = aws_secretsmanager_secret.jwt_secret.arn }
output "db_password_secret_arn" { value = aws_secretsmanager_secret.db_password.arn }
