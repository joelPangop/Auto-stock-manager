output "endpoint"    { value = aws_db_instance.mysql.endpoint }
output "address"     { value = aws_db_instance.mysql.address }
output "port"        { value = aws_db_instance.mysql.port }
output "db_name"     { value = aws_db_instance.mysql.db_name }

output "tunnel_command" {
  description = "Commande tunnel SSH → RDS via bastion (remplacez BASTION_IP)"
  value       = "ssh -i ~/.ssh/id_rsa -L 3306:${aws_db_instance.mysql.address}:3306 ec2-user@<BASTION_IP> -N"
}
