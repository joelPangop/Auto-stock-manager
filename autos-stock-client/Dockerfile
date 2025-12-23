# ---------- build stage ----------
FROM node:20 AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Fix OpenSSL 3 / webpack (si Angular/webpack en a besoin)
ENV NODE_OPTIONS=--openssl-legacy-provider

# Supprimer un éventuel build précédent
RUN rm -rf build

# Build Angular (prod)
RUN npm run build

# ---------- nginx stage ----------
FROM nginx:alpine
# ton nginx.conf existe déjà dans le projet
COPY nginx.conf /etc/nginx/conf.d/default.conf

# ⚠️ Angular output folder (d'après ton log)
COPY --from=build /app/dist/autos-stock-client/ /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
