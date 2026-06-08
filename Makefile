# ==============================================================
# Makefile racine — Auto Stock Manager
# Usage : make <cible>
# ==============================================================

TF_DEV  = terraform/environments/dev
TF_PROD = terraform/environments/prod
DC      = docker-compose -f docker-compose/docker-compose.yml

# Couleurs
CYAN  = \033[0;36m
GREEN = \033[0;32m
YELLOW= \033[0;33m
RED   = \033[0;31m
RESET = \033[0m

.PHONY: help
help: ## Afficher l'aide
	@echo ""
	@echo "  $(CYAN)Auto Stock Manager$(RESET) — commandes disponibles"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) \
		| sort \
		| awk 'BEGIN {FS = ":.*?## "}; {printf "  $(CYAN)%-28s$(RESET) %s\n", $$1, $$2}'
	@echo ""

# ──────────────────────────────────────────────
# DOCKER COMPOSE (local)
# ──────────────────────────────────────────────

.PHONY: up
up: ## Démarrer tous les conteneurs (LocalStack + MySQL + Backend + Frontend)
	$(DC) up -d
	@echo "$(GREEN)✔ Environnement local démarré$(RESET)"

.PHONY: down
down: ## Arrêter tous les conteneurs
	$(DC) down
	@echo "$(GREEN)✔ Environnement local arrêté$(RESET)"

.PHONY: restart
restart: ## Redémarrer tous les conteneurs
	$(DC) down && $(DC) up -d

.PHONY: logs
logs: ## Suivre les logs de tous les conteneurs
	$(DC) logs -f

.PHONY: logs-backend
logs-backend: ## Suivre les logs du backend uniquement
	$(DC) logs -f autostockmanager

.PHONY: logs-frontend
logs-frontend: ## Suivre les logs du frontend admin (Angular) uniquement
	$(DC) logs -f autostockclient

.PHONY: logs-customer
logs-customer: ## Suivre les logs du portail client (React) uniquement
	$(DC) logs -f autostockcustomer

.PHONY: ps
ps: ## Statut des conteneurs
	$(DC) ps

.PHONY: build-docker
build-docker: ## Rebuilder toutes les images Docker
	$(DC) build

.PHONY: deploy-backend
deploy-backend: ## Rebuilder et redéployer le backend (compile + image + conteneur)
	cd Autos-stock-manager-services && ./mvnw package -DskipTests -q
	$(DC) build autostockmanager
	$(DC) up -d --force-recreate autostockmanager
	@echo "$(GREEN)✔ Backend redéployé$(RESET)"

.PHONY: deploy-frontend
deploy-frontend: ## Rebuilder et redéployer le frontend (npm build + image + conteneur)
	cd autos-stock-client && npm run build
	$(DC) build autostockclient
	$(DC) up -d --force-recreate autostockclient
	@echo "$(GREEN)✔ Frontend redéployé$(RESET)"

.PHONY: deploy-customer
deploy-customer: ## Rebuilder et redéployer le portail client React (Ted Auto)
	cd autostock-customer && npm run build
	$(DC) build autostockcustomer
	$(DC) up -d --force-recreate autostockcustomer
	@echo "$(GREEN)✔ Portail client redéployé → http://localhost:3001$(RESET)"

.PHONY: deploy-all
deploy-all: deploy-backend deploy-frontend deploy-customer ## Rebuilder et redéployer backend + admin + client
	@echo "$(GREEN)✔ Déploiement complet terminé$(RESET)"

# ──────────────────────────────────────────────
# TERRAFORM — DEV (LocalStack)
# ──────────────────────────────────────────────

.PHONY: tf-dev-init
tf-dev-init: ## [Terraform DEV] Initialiser le dossier dev
	@echo "$(CYAN)→ Terraform DEV : init$(RESET)"
	cd $(TF_DEV) && terraform init

.PHONY: tf-dev-plan
tf-dev-plan: ## [Terraform DEV] Planifier les changements (LocalStack)
	@echo "$(CYAN)→ Terraform DEV : plan$(RESET)"
	cd $(TF_DEV) && terraform plan

.PHONY: tf-dev-apply
tf-dev-apply: ## [Terraform DEV] Appliquer sur LocalStack (auto-approve)
	@echo "$(CYAN)→ Terraform DEV : apply$(RESET)"
	cd $(TF_DEV) && terraform apply -auto-approve
	@echo "$(GREEN)✔ Ressources DEV appliquées sur LocalStack$(RESET)"

.PHONY: tf-dev-destroy
tf-dev-destroy: ## [Terraform DEV] Détruire les ressources LocalStack
	@echo "$(RED)→ Terraform DEV : destroy$(RESET)"
	cd $(TF_DEV) && terraform destroy -auto-approve
	@echo "$(YELLOW)✔ Ressources DEV supprimées de LocalStack$(RESET)"

.PHONY: tf-dev-output
tf-dev-output: ## [Terraform DEV] Afficher les outputs (URLs, ARNs…)
	cd $(TF_DEV) && terraform output

.PHONY: tf-dev-refresh
tf-dev-refresh: ## [Terraform DEV] Rafraîchir l'état sans modifier l'infra
	cd $(TF_DEV) && terraform refresh

# ──────────────────────────────────────────────
# TERRAFORM — PROD (AWS réel)
# ──────────────────────────────────────────────

.PHONY: tf-prod-init
tf-prod-init: ## [Terraform PROD] Initialiser le dossier prod
	@echo "$(CYAN)→ Terraform PROD : init$(RESET)"
	cd $(TF_PROD) && terraform init

.PHONY: tf-prod-plan
tf-prod-plan: ## [Terraform PROD] Planifier les changements (AWS réel)
	@echo "$(CYAN)→ Terraform PROD : plan$(RESET)"
	cd $(TF_PROD) && terraform plan

.PHONY: tf-prod-apply
tf-prod-apply: _confirm-prod ## [Terraform PROD] Appliquer sur AWS réel (confirmation requise)
	@echo "$(CYAN)→ Terraform PROD : apply$(RESET)"
	cd $(TF_PROD) && terraform apply
	@echo "$(GREEN)✔ Infra PROD appliquée sur AWS$(RESET)"

.PHONY: tf-prod-destroy
tf-prod-destroy: _confirm-prod ## [Terraform PROD] ⚠ DÉTRUIRE toute l'infra AWS (confirmation requise)
	@echo "$(RED)→ Terraform PROD : destroy$(RESET)"
	cd $(TF_PROD) && terraform destroy
	@echo "$(YELLOW)✔ Infra PROD détruite$(RESET)"

.PHONY: tf-prod-output
tf-prod-output: ## [Terraform PROD] Afficher les outputs (DNS ALB, IP bastion, tunnel RDS…)
	cd $(TF_PROD) && terraform output

.PHONY: tf-prod-refresh
tf-prod-refresh: ## [Terraform PROD] Rafraîchir l'état depuis AWS
	cd $(TF_PROD) && terraform refresh

.PHONY: tf-prod-show
tf-prod-show: ## [Terraform PROD] Afficher l'état actuel de l'infra
	cd $(TF_PROD) && terraform show

# ──────────────────────────────────────────────
# TERRAFORM — Commandes communes
# ──────────────────────────────────────────────

.PHONY: tf-fmt
tf-fmt: ## Formater tous les fichiers .tf du projet
	@echo "$(CYAN)→ Formatage Terraform$(RESET)"
	terraform fmt -recursive terraform/
	@echo "$(GREEN)✔ Fichiers .tf formatés$(RESET)"

.PHONY: tf-validate
tf-validate: ## Valider la syntaxe de dev et prod
	@echo "$(CYAN)→ Validation DEV$(RESET)"
	cd $(TF_DEV) && terraform init -backend=false -input=false > /dev/null && terraform validate
	@echo "$(CYAN)→ Validation PROD$(RESET)"
	cd $(TF_PROD) && terraform init -backend=false -input=false > /dev/null && terraform validate
	@echo "$(GREEN)✔ Syntaxe Terraform valide$(RESET)"

.PHONY: tf-init-all
tf-init-all: tf-dev-init tf-prod-init ## Initialiser dev et prod en une seule commande

# ──────────────────────────────────────────────
# CONNEXION SSH / TUNNEL RDS (PROD)
# ──────────────────────────────────────────────

.PHONY: ssh-bastion
ssh-bastion: ## Se connecter au bastion SSH (prod)
	@BASTION_IP=$$(cd $(TF_PROD) && terraform output -raw bastion_ip 2>/dev/null); \
	if [ -z "$$BASTION_IP" ]; then \
		echo "$(RED)✖ Bastion introuvable. Avez-vous appliqué terraform prod ?$(RESET)"; \
		exit 1; \
	fi; \
	echo "$(CYAN)→ Connexion SSH au bastion $$BASTION_IP$(RESET)"; \
	ssh -i ~/.ssh/id_rsa ec2-user@$$BASTION_IP

.PHONY: tunnel-rds
tunnel-rds: ## Ouvrir le tunnel SSH → RDS sur localhost:3306 (prod)
	@BASTION_IP=$$(cd $(TF_PROD) && terraform output -raw bastion_ip 2>/dev/null); \
	RDS_ADDR=$$(cd $(TF_PROD) && terraform output -raw rds_endpoint 2>/dev/null | cut -d: -f1); \
	if [ -z "$$BASTION_IP" ] || [ -z "$$RDS_ADDR" ]; then \
		echo "$(RED)✖ Outputs introuvables. Avez-vous appliqué terraform prod ?$(RESET)"; \
		exit 1; \
	fi; \
	echo "$(CYAN)→ Tunnel SSH : localhost:3306 → $$RDS_ADDR:3306 via $$BASTION_IP$(RESET)"; \
	echo "$(YELLOW)  Connectez votre client MySQL sur localhost:3306$(RESET)"; \
	echo "$(YELLOW)  Ctrl+C pour fermer le tunnel$(RESET)"; \
	ssh -i ~/.ssh/id_rsa -L 3306:$$RDS_ADDR:3306 ec2-user@$$BASTION_IP -N

# ──────────────────────────────────────────────
# ECR — Push des images Docker (PROD)
# ──────────────────────────────────────────────

IMAGE_TAG ?= latest

.PHONY: ecr-login
ecr-login: ## S'authentifier auprès d'ECR (prod)
	@AWS_REGION=$$(cd $(TF_PROD) && terraform output -raw aws_region 2>/dev/null || echo "us-east-1"); \
	BACKEND_URL=$$(cd $(TF_PROD) && terraform output -raw ecr_backend 2>/dev/null); \
	aws ecr get-login-password --region $$AWS_REGION | \
		docker login --username AWS --password-stdin $$BACKEND_URL
	@echo "$(GREEN)✔ Authentifié sur ECR$(RESET)"

.PHONY: ecr-push
ecr-push: ecr-login ## Builder et pousser les images backend + frontend sur ECR (prod)
	@BACKEND_URL=$$(cd $(TF_PROD) && terraform output -raw ecr_backend 2>/dev/null); \
	FRONTEND_URL=$$(cd $(TF_PROD) && terraform output -raw ecr_frontend 2>/dev/null); \
	echo "$(CYAN)→ Build & push backend : $$BACKEND_URL:$(IMAGE_TAG)$(RESET)"; \
	docker build -t $$BACKEND_URL:$(IMAGE_TAG) ./Autos-stock-manager-services && \
	docker push $$BACKEND_URL:$(IMAGE_TAG); \
	echo "$(CYAN)→ Build & push frontend : $$FRONTEND_URL:$(IMAGE_TAG)$(RESET)"; \
	docker build -t $$FRONTEND_URL:$(IMAGE_TAG) ./autos-stock-client && \
	docker push $$FRONTEND_URL:$(IMAGE_TAG); \
	echo "$(GREEN)✔ Images poussées sur ECR$(RESET)"

# ──────────────────────────────────────────────
# Cibles internes
# ──────────────────────────────────────────────

.PHONY: _confirm-prod
_confirm-prod:
	@echo "$(RED)⚠  ATTENTION : cette commande modifie l'infrastructure AWS RÉELLE$(RESET)"
	@read -p "Tapez 'yes' pour confirmer : " CONFIRM; \
	if [ "$$CONFIRM" != "yes" ]; then \
		echo "$(YELLOW)Annulé.$(RESET)"; exit 1; \
	fi
