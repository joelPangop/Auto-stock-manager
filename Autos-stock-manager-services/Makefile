# Makefile for Autos Stock Manager Spring Boot Application

# Variables
APP_NAME = Autos-stock-manager
VERSION = 1.0-SNAPSHOT
JAR_NAME = $(APP_NAME)-$(VERSION).jar
DOCKER_IMAGE = autostock/$(APP_NAME):$(VERSION)
DOCKER_IMAGE_LATEST = autostock/$(APP_NAME):latest

# Maven commands
MVN = mvn
MAVEN_OPTS = -Dmaven.test.skip=false

# Docker commands
DOCKER = docker
DOCKER_COMPOSE = docker-compose

# Default target
.PHONY: help
help: ## Show this help message
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# Build targets
.PHONY: clean
clean: ## Clean the project
	$(MVN) clean

.PHONY: compile
compile: ## Compile the project
	$(MVN) compile

.PHONY: build
build: clean ## Build the project (clean + package)
	$(MVN) package -DskipTests

.PHONY: build-with-tests
build-with-tests: clean ## Build the project with tests
	$(MVN) package

.PHONY: install
install: ## Install the project in local repository
	$(MVN) install

# Test targets
.PHONY: test
test: ## Run tests
	$(MVN) test

.PHONY: test-coverage
test-coverage: ## Run tests with coverage report
	$(MVN) verify

.PHONY: integration-test
integration-test: ## Run integration tests
	$(MVN) verify -Pintegration-tests


1# Quality targets
.PHONY: sonar
sonar: ## Run SonarQube analysis
	$(MVN) sonar:sonar

.PHONY: dependency-check
dependency-check: ## Check for dependency updates
	$(MVN) versions:display-dependency-updates

# Run targets
.PHONY: run
run: ## Run the application locally
	$(MVN) spring-boot:run

.PHONY: run-dev
run-dev: ## Run the application in development mode
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=dev

.PHONY: run-prod
run-prod: build ## Run the built JAR file
	java -jar target/$(JAR_NAME)

# Docker targets
.PHONY: docker-build
docker-build: ## Build Docker image
	$(DOCKER) build -t $(DOCKER_IMAGE) .
	$(DOCKER) tag $(DOCKER_IMAGE) $(DOCKER_IMAGE_LATEST)

.PHONY: docker-run
docker-run: ## Run the application in Docker container
	$(DOCKER) run -p 8080:8080 -p 5005:5005 $(DOCKER_IMAGE_LATEST)

.PHONY: docker-run-detached
docker-run-detached: ## Run the application in Docker container (detached)
	$(DOCKER) run -d -p 8080:8080 -p 5005:5005 --name $(APP_NAME) $(DOCKER_IMAGE_LATEST)

.PHONY: docker-stop
docker-stop: ## Stop the Docker container
	$(DOCKER) stop $(APP_NAME) || true
	$(DOCKER) rm $(APP_NAME) || true

.PHONY: docker-clean
docker-clean: ## Remove Docker image and containers
	$(DOCKER) stop $(APP_NAME) || true
	$(DOCKER) rm $(APP_NAME) || true
	$(DOCKER) rmi $(DOCKER_IMAGE) $(DOCKER_IMAGE_LATEST) || true

.PHONY: docker-logs
docker-logs: ## Show Docker container logs
	$(DOCKER) logs -f $(APP_NAME)

# Development targets
.PHONY: dev-setup
dev-setup: ## Setup development environment
	$(MVN) dependency:go-offline
	@echo "Development environment setup complete!"

.PHONY: format
format: ## Format code (if you have a formatter configured)
	$(MVN) spotless:apply || echo "No formatter configured"

.PHONY: lint
lint: ## Run code linting (if configured)
	$(MVN) checkstyle:check || echo "No linting configured"

# Utility targets
.PHONY: deps
deps: ## Download dependencies
	$(MVN) dependency:resolve

.PHONY: tree
tree: ## Show dependency tree
	$(MVN) dependency:tree

.PHONY: effective-pom
effective-pom: ## Show effective POM
	$(MVN) help:effective-pom

# Quick targets
.PHONY: quick-build
quick-build: ## Quick build without tests
	$(MVN) package -DskipTests -q

.PHONY: quick-run
quick-run: quick-build ## Quick build and run
	java -jar target/$(JAR_NAME)

# Complete workflow targets
.PHONY: ci
ci: clean build-with-tests test-coverage ## Complete CI pipeline
	@echo "CI pipeline completed successfully!"

.PHONY: deploy-local
deploy-local: build docker-build docker-stop docker-run-detached ## Build and deploy locally
	@echo "Application deployed locally!"
	@echo "Access the application at: http://localhost:8080"
	@echo "Debug port available at: 5005"

# Maintenance targets
.PHONY: logs-clean
logs-clean: ## Clean application logs
	rm -rf logs/*

.PHONY: target-clean
target-clean: ## Clean target directory
	rm -rf target/

.PHONY: full-clean
full-clean: clean target-clean logs-clean ## Complete cleanup
	@echo "Full cleanup completed!"

# Info targets
.PHONY: info
info: ## Show project information
	@echo "Project: $(APP_NAME)"
	@echo "Version: $(VERSION)"
	@echo "JAR: $(JAR_NAME)"
	@echo "Docker Image: $(DOCKER_IMAGE)"

.PHONY: status
status: ## Show application status
	@echo "Checking application status..."
	@curl -f http://localhost:8080/actuator/health || echo "Application not running or health endpoint not available"clade
