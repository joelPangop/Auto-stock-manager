#!/bin/bash
set -e

cd /root/Auto-stock-manager

echo "📥 Git pull..."
git pull origin master

echo "🐳 Rebuild et relance des conteneurs..."
cd docker-compose
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build

echo "✅ Déploiement terminé !"
