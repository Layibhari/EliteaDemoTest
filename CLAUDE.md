# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

Both Maven and Gradle wrappers are supported. Maven is preferred for most tasks.

```bash
# Run the application (default: H2 in-memory DB, port 8080)
./mvnw spring-boot:run
./gradlew bootRun

# Build
./mvnw clean install
./gradlew build

# Run all tests
./mvnw test
./gradlew test

# Run a single test class
./mvnw test -Dtest=OwnerControllerTests
./gradlew test --tests "*.OwnerControllerTests"

# Rebuild CSS from SCSS (required after editing src/main/scss/)
./mvnw package -P css

# Build a container image
./mvnw spring-boot:build-image
```

## Database Profiles

Default profile uses H2 (in-memory). Switch with `--spring.profiles.active`:

```bash
# MySQL (requires docker-compose up mysql)
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql

# PostgreSQL (requires docker-compose up postgres)
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

MySQL and PostgreSQL connection details can be overridden via environment variables — see [application-mysql.properties](air-file://p3i8oqofm9ei5u3rm9hj/Users/dileepa.jayakody/Documents/PetClinic_2/spring-petclinic-v2/src/main/resources/application-mysql.properties?type=file&root=%252F) and [application-postgres.properties](air-file://p3i8oqofm9ei5u3rm9hj/Users/dileepa.jayakody/Documents/PetClinic_2/spring-petclinic-v2/src/main/resources/application-postgres.properties?type=file&root=%252F).

## Architecture

**No service layer.** Controllers inject repositories directly. The layering is: Controller → Repository → Entity.

**Package structure** under `src/main/java/org/springframework/samples/petclinic/`:
- `model` — Base entity hierarchy: `BaseEntity` → `NamedEntity` → `Person`. All JPA entities extend these.
- `owner` — Owner, Pet, PetType, Visit entities + their controllers, repositories, validator, and formatter.
- `vet` — Vet and Specialty entities + VetController and VetRepository. Vet list results are cached with Caffeine (`@Cacheable("vets")`).
- `system` — CacheConfiguration, WebConfiguration (i18n/locale), WelcomeController, CrashController, and GraalVM native image runtime hints.

**Data access:** Spring Data JPA with method-name query derivation (e.g., `findByLastNameStartingWith`). SQL schema and seed data live in `src/main/resources/db/<database>/` and are loaded at startup via `spring.sql.init`.

**Frontend:** Thymeleaf templates in `src/main/resources/templates/`. CSS is compiled from SCSS sources in `src/main/scss/` — never edit the generated CSS directly.

**Testing:** `@WebMvcTest` with MockMvc for controller-layer tests; `PetClinicIntegrationTests` runs full-stack against H2; `MySqlIntegrationTests` and `PostgresIntegrationTests` use Testcontainers / Docker Compose.

## Development Rules

- Edit SCSS (`src/main/scss/`), not the generated CSS under `src/main/resources/static/resources/css/`.
- Preserve H2 compatibility unless a change explicitly targets a specific database profile.
- Add or update tests when changing controller logic, validation, or persistence behavior.
- Keep changes aligned with existing Spring MVC conventions; prefer small local edits over broad refactors.
