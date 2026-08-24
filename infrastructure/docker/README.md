# NexusBank Docker Infrastructure

This directory contains the local infrastructure required to run NexusBank services.

## Services

| Service | Container | Port |
|---|---|---|
| PostgreSQL | nexusbank-postgres | 5432 |
| Redis | nexusbank-redis | 6379 |

## Start Infrastructure

From the project root:

```bash
docker compose -f infrastructure/docker/docker-compose.yml up -d