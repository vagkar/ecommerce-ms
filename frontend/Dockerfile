# Stage 1: Build
FROM node:22-alpine AS build
WORKDIR /app

# Install dependencies first — cached unless package.json changes
COPY package*.json ./
RUN npm ci --fetch-retries=5 --fetch-retry-mintimeout=20000 --fetch-retry-maxtimeout=120000

COPY . .
RUN npm run build

# Stage 2: Serve — production-grade static file server with SPA routing support
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80