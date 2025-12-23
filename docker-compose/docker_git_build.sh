#!/bin/bash

# Se placer dans le dossier du script
cd "$(dirname "$0")" || exit 1

# Remonter à la racine du projet
cd .. || exit 1

echo "📥 Pull Git..."
#git pull origin master

echo "⛔️ Arrêt des conteneurs..."
docker-compose down

# Obtenir l'IP publique de l'EC2
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)

# Générer le fichier .env avec FRONTEND_URL
echo "FRONTEND_URL=http://$PUBLIC_IP:3000" > .env
echo "✅ Fichier .env généré avec FRONTEND_URL=http://$PUBLIC_IP:3000"

# === Build du frontend React ===
CLIENT_DIR="./autos-stock-client"

echo "🌍 IP Publique EC2 : $PUBLIC_IP"
echo "🛠️ Build autostockclient avec REACT_APP_API_URL=http://$PUBLIC_IP:8080"

docker build \
  --build-arg REACT_APP_API_URL=http://$PUBLIC_IP:8080 \
  -t autostockclient \
  -f ./autos-stock-client/Dockerfile \
  --no-cache \
  autos-stock-client || exit 1

# === Build du backend Spring Boot ===
echo "🧪 Build backend..."
cd ./autos-stock-manager-services || exit 1
chmod +x mvnw
./mvnw clean package -DskipTests || exit 1
cd ..

# === Docker Compose ===
echo "🐳 Lancement des conteneurs..."
docker-compose up -buil

echo "✅ Déploiement terminé avec succès !"
