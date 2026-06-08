# ============================================================
# Module : Bastion Host (EC2 t3.micro dans subnet public)
# SSH Tunneling → RDS MySQL en subnet privé
# ============================================================

# Dernière AMI Amazon Linux 2023 (us-east-1 / eu-west-1 etc.)
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Clé SSH : on importe la clé publique fournie
resource "aws_key_pair" "bastion" {
  key_name   = "${var.project}-bastion-key"
  public_key = var.ssh_public_key

  tags = var.tags
}

resource "aws_instance" "bastion" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = "t3.micro"
  subnet_id              = var.public_subnet_id
  vpc_security_group_ids = [var.sg_bastion_id]
  key_name               = aws_key_pair.bastion.key_name

  # Empêcher la suppression accidentelle
  disable_api_termination = false

  user_data = <<-EOF
    #!/bin/bash
    yum update -y
    yum install -y mysql
    echo "Bastion ${var.project} prêt" > /etc/motd
  EOF

  tags = merge(var.tags, { Name = "${var.project}-bastion" })
}

# IP Elastic pour le bastion (IP stable)
resource "aws_eip" "bastion" {
  instance = aws_instance.bastion.id
  domain   = "vpc"

  tags = merge(var.tags, { Name = "${var.project}-bastion-eip" })
}
