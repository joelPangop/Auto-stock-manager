#!/bin/sh

echo "⌛ Attente que MySQL soit prêt..."

DB_HOST=${DB_HOST:-localhost}
DB_USER=${DB_USER:-root}
DB_PASSWORD=${DB_PASSWORD:-password}

until mysqladmin ping -h mysql -u"$SPRING_DATASOURCE_USERNAME" -p"$SPRING_DATASOURCE_PASSWORD" --silent; do
  echo "⏳ MySQL ($DB_HOST) n'est pas encore prêt. Nouvelle tentative dans 5s..."
  sleep 5
done

echo "✅ MySQL est prêt. Lancement de l'application Spring Boot..."

echo "🧪 Build backend..."
cd ../autos-stock-manager-services || exit 1
chmod +x mvnw
./mvnw clean package -DskipTests || exit 1
cd ..

# ✅ Ne pas redéclarer l’agent JDWP ici si JAVA_TOOL_OPTIONS le contient déjà
exec java -jar app.jar
