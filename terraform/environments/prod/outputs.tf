output "alb_url" {
  description = "URL publique de l'application"
  value       = "http://${module.alb.alb_dns_name}"
}

output "bastion_ip" {
  description = "IP publique du bastion SSH"
  value       = module.bastion.public_ip
}

output "ssh_bastion" {
  description = "Commande SSH vers le bastion"
  value       = module.bastion.ssh_command
}

output "rds_tunnel" {
  description = "Tunnel SSH → RDS via bastion"
  value       = "ssh -i ~/.ssh/id_rsa -L 3306:${module.database.address}:3306 ec2-user@${module.bastion.public_ip} -N"
}

output "rds_endpoint" { value = module.database.endpoint }

output "ecr_backend" { value = module.ecr.backend_repo_url }
output "ecr_frontend" { value = module.ecr.frontend_repo_url }

output "ecs_cluster" { value = module.ecs.cluster_name }

output "push_commands" {
  description = "Commandes pour pousser les images vers ECR"
  value       = <<-EOT
    # 1. Authentification ECR
    aws ecr get-login-password --region ${var.aws_region} | \
      docker login --username AWS --password-stdin ${module.ecr.backend_repo_url}

    # 2. Build & push backend
    docker build -t ${module.ecr.backend_repo_url}:${var.image_tag} ./Autos-stock-manager-services
    docker push ${module.ecr.backend_repo_url}:${var.image_tag}

    # 3. Build & push frontend
    docker build -t ${module.ecr.frontend_repo_url}:${var.image_tag} ./autos-stock-client
    docker push ${module.ecr.frontend_repo_url}:${var.image_tag}
  EOT
}
