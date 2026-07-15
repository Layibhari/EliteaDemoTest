# ---- build stage ----
FROM node:20-slim AS builder
WORKDIR /build

COPY package*.json ./
RUN npm ci --omit=dev

COPY server.js app.js inventory.js ./

# ---- runtime stage ----
FROM node:20-slim

RUN groupadd --system appuser && useradd --system --gid appuser appuser

WORKDIR /app
COPY --from=builder /build/node_modules ./node_modules
COPY --from=builder /build/package.json ./
COPY --from=builder /build/*.js ./
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD node -e "require('http').get('http://localhost:3000/health', (r) => process.exit(r.statusCode === 200 ? 0 : 1)).on('error', () => process.exit(1))"

ENTRYPOINT ["node", "server.js"]
