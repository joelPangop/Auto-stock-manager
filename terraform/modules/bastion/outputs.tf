output "public_ip"    { value = aws_eip.bastion.public_ip }
output "instance_id"  { value = aws_instance.bastion.id }

output "ssh_command" {
  description = "Commande SSH directe vers le bastion"
  value       = "ssh -i ~/.ssh/id_rsa ec2-user@${aws_eip.bastion.public_ip}"
}

output "tunnel_command" {
  description = "Tunnel SSH pour accéder à RDS via le bastion (remplacez RDS_ENDPOINT)"
  value       = "ssh -i ~/.ssh/id_rsa -L 3306:<RDS_ENDPOINT>:3306 ec2-user@${aws_eip.bastion.public_ip} -N"
}
