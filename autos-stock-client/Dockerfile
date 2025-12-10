# ---------- Stage 1: Build React App ----------
FROM node:20 AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Supprimer un éventuel build précédent
RUN rm -rf build

# 🛠 Build et vérifier si build/ est créé
#RUN REACT_APP_API_URL=$REACT_APP_API_URL npm run build && ls -al build
ARG REACT_APP_API_URL
ENV REACT_APP_API_URL=$REACT_APP_API_URL
RUN npm run build   # génère /app/build

# ---------- Stage 2: Serve with Nginx ----------
FROM nginx:alpine

COPY --from=build /app/build /usr/share/nginx/html
#COPY ./public/env-config.js /usr/share/nginx/html/env-config.js
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
