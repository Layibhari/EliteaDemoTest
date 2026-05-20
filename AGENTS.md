# AGENTS.md

## Overview

This repository is the Spring Boot version of Spring PetClinic.

- Language: Java 17+
- Framework: Spring Boot
- Build tools: Maven wrapper (`./mvnw`) and Gradle wrapper (`./gradlew`)
- Default runtime port: `8080`
- Default database: in-memory H2
- Optional profiles: `mysql`, `postgres`

## Repository Shape

- Main application class: `src/main/java/org/springframework/samples/petclinic/PetClinicApplication.java`
- Main source root: `src/main/java/org/springframework/samples/petclinic`
- Main resources: `src/main/resources`
- Tests: `src/test/java/org/springframework/samples/petclinic`
- SCSS source: `src/main/scss`
- Generated CSS lives under `src/main/resources/static/resources/css`

Primary feature areas are organized by package:

- `owner`
- `vet`
- `system`
- `model`

## Common Commands

Prefer Maven unless there is a reason to use Gradle, since the README documents Maven-first workflows.

### Run locally

```bash
./mvnw spring-boot:run
```

Alternative:

```bash
./gradlew bootRun
```

### Run tests

```bash
./mvnw test
```

Alternative:

```bash
./gradlew test
```

### Package

```bash
./mvnw package
```

### Rebuild CSS after SCSS changes

```bash
./mvnw package -P css
```

## Working Rules

- Keep changes aligned with existing Spring MVC and package conventions.
- Prefer small, local edits over broad refactors.
- Preserve support for the default H2 setup unless the task explicitly targets another profile.
- When editing UI styles, update SCSS sources rather than hand-editing generated CSS unless the task specifically requires otherwise.
- Add or update focused tests when changing controller behavior, validation, formatting, or persistence behavior.

## Testing Guidance

- For targeted Java changes, run the narrowest relevant test class first.
- Run the full Maven test suite before finishing larger changes when feasible.
- Integration-oriented test entry points already exist in:
  - `PetClinicIntegrationTests`
  - `MySqlIntegrationTests`
  - `PostgresIntegrationTests`

## Notes For Agents

- Use semantic code search when you need to locate behavior across the codebase.
- Read files in chunks when inspecting larger sources.
- Do not assume frontend assets are source-of-truth if corresponding SCSS exists.
