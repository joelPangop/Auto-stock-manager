#!/bin/bash
set -e

# Se placer à la racine du projet
cd /root/Auto-stock-manager

echo "📥 Git pull..."
git pull origin master

echo "🐳 Rebuild et relance des conteneurs..."
docker compose up -d --build

echo "✅ Déploiement terminé !"
