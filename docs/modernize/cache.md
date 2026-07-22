# Externalize cache to Azure Cache for Redis (local Caffeine fallback)

This document explains how to run the application locally using an in-memory Caffeine cache and how to enable an external Redis cache (for Azure Cache for Redis) in non-local environments.

## Default (local) - Caffeine

By default (no `redis` profile active) the application uses an in-memory Caffeine cache. To explicitly select the local cache profile:

- Run the application with the `local` profile active:
  - Using Maven: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
  - Using Gradle: `./gradlew bootRun --args='--spring.profiles.active=local'`

No Redis instance is required for local development.

## Enable Redis (external)

To enable Redis and use Azure Cache for Redis (or any Redis instance), activate the `redis` Spring profile and provide connection settings via environment variables or properties.

- Environment variables (recommended):
  - `REDIS_HOST` (defaults to `localhost`)
  - `REDIS_PORT` (defaults to `6379`)

- Example (Linux/macOS):
  - `REDIS_HOST=redis.example.com REDIS_PORT=6380 ./mvnw spring-boot:run -Dspring-boot.run.profiles=redis`

- Application properties (alternatively):
  - Edit `src/main/resources/application-redis.properties` or set `spring.redis.host` and `spring.redis.port`.

## Azure Cache for Redis and Key Vault guidance

- Create an Azure Cache for Redis instance following Azure documentation.
- For production credentials and connection strings, prefer using Azure Key Vault and inject secrets into your deployment environment rather than hardcoding values in properties files.
- Set the `REDIS_HOST` and `REDIS_PORT` environment variables in your deployment to point to the Azure Cache for Redis host and port. If using TLS/SSL or requiring password/auth, extend the configuration to include `spring.redis.password` and TLS-related settings and read them from Key Vault.

## Notes for maintainers

- This change adds a new profile-aware cache configuration in `src/main/java/org/springframework/samples/petclinic/config/CacheConfig.java`.
- Local default cache uses Caffeine and registers the `vets` cache used by `@Cacheable("vets")` in the codebase.
- Redis profile uses Lettuce and configures a `RedisCacheManager` wired to `spring.redis.*` properties.
- Build files were updated to include `spring-boot-starter-data-redis` and `lettuce-core`.

