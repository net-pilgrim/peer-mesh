# Multi-stage build for Svelte 5 PWA
FROM node:22-alpine AS builder
WORKDIR /app

# Cache node modules
COPY package*.json ./
RUN npm ci || npm install

# Build static bundle
COPY . .
RUN npm run build

# Production Server: Ultra-lean Nginx Alpine image
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
