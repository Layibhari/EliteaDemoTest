# PetClinic Inventory Service

A small, self-contained [Express](https://expressjs.com/) microservice that lives alongside the
Spring PetClinic Java app in this monorepo. It has no database and no dependency on the Java app —
it tracks in-memory stock levels for clinic medical supplies (seeded with a few sample items).

## Endpoints

| Method | Path                            | Description                                                     |
|--------|----------------------------------|-------------------------------------------------------------------|
| GET    | `/health`                        | Liveness check, returns `{"status": "ok"}`.                      |
| GET    | `/inventory/low-stock`           | Lists items at or below their reorder threshold.                  |
| GET    | `/inventory/:itemId`             | Returns the current stock record for an item (404 if unknown).    |
| POST   | `/inventory/:itemId/adjust`      | Adjusts stock by `{"delta": <integer>}` (positive or negative).   |

### Example requests

```bash
curl http://localhost:3000/inventory/rabies-vaccine

curl -X POST http://localhost:3000/inventory/rabies-vaccine/adjust \
  -H "Content-Type: application/json" \
  -d '{"delta": -3}'

curl http://localhost:3000/inventory/low-stock
```

## Running locally

```bash
cd node-service
npm install
npm start
```

The service listens on <http://localhost:3000>.

## Running tests

```bash
cd node-service
npm install
npm test
```

Tests use [Jest](https://jestjs.io/) and [Supertest](https://github.com/ladjs/supertest) and cover
all endpoints, including the 400/404/409 error branches in the stock-adjustment logic.

## Building the container

```bash
cd node-service
docker build -t petclinic-inventory-service .
docker run -p 3000:3000 petclinic-inventory-service
```
