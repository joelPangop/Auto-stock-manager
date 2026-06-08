variable "project" {
  description = "Nom du projet (préfixe pour toutes les ressources)"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR du VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Liste des zones de disponibilité AWS"
  type        = list(string)
}

variable "bastion_allowed_cidrs" {
  description = "CIDRs autorisés à SSH sur le bastion (ex: votre IP publique /32)"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "tags" {
  description = "Tags communs appliqués à toutes les ressources"
  type        = map(string)
  default     = {}
}
