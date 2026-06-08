variable "project" {
  type = string
}

variable "public_subnet_id" {
  type = string
}

variable "sg_bastion_id" {
  type = string
}

variable "ssh_public_key" {
  type        = string
  description = "Contenu de la clé publique SSH (ex: ~/.ssh/id_rsa.pub)"
}

variable "tags" {
  type    = map(string)
  default = {}
}
