# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Spring PetClinic — a Spring Boot 4.0 web application (server-rendered Thymeleaf UI, no REST API)
for managing pet owners, their pets, visits, and veterinarians. Java 17, builds with both Maven
and Gradle (keep both build files in sync when changing dependencies).

## Common commands

Maven (`./mvnw`) and Gradle (`./gradlew`) are interchangeable. Maven equivalents shown second.

```bash
# Run the app (H2 in-memory DB, seeded at startup); UI at http://localhost:8080
./gradlew bootRun                 # ./mvnw spring-boot:run

# Full build + tests
./gradlew build                   # ./mvnw verify

# Run all tests
./gradlew test                    # ./mvnw test

# Run a single test class / method
./gradlew test --tests OwnerControllerTests
./gradlew test --tests "OwnerControllerTests.testProcessCreationFormSuccess"
./mvnw test -Dtest=OwnerControllerTests
./mvnw test -Dtest='OwnerControllerTests#testProcessCreationFormSuccess'

# Recompile CSS after editing src/main/scss/petclinic.scss (Maven only — no Gradle task)
./mvnw package -P css
```

The H2 console is at `/h2-console`; the JDBC URL (`jdbc:h2:mem:<uuid>`) is printed to the
console at startup.

### Database profiles

Default is H2. Switch with `spring.profiles.active=mysql` or `=postgres`
(see `application-mysql.properties` / `application-postgres.properties`). `docker compose up mysql`
or `up postgres` starts a matching container.

## Code style — must pass before build succeeds

`spring-javaformat` and `nohttp`/checkstyle run at the Maven `validate` phase and as Gradle
`checkstyle*`/`format*` tasks; a formatting violation **fails the build**, not just a warning.
Run `./mvnw spring-javaformat:apply` (or `./gradlew format`) to auto-format before committing.
Indentation is tabs. Commit messages must include a `Signed-off-by` trailer (DCO).

## Architecture

Code is organized **package-by-feature** under `org.springframework.samples.petclinic`, not by
layer. Each feature package holds its own entities, controller, repository, and validators:

- `owner/` — `Owner`, `Pet`, `PetType`, `Visit` entities plus `OwnerController`, `PetController`,
  `VisitController`, their Spring Data repositories, and `PetValidator` / `PetTypeFormatter`.
- `vet/` — `Vet`, `Specialty`, the `Vets` JAXB wrapper (for XML/Actuator views), `VetController`,
  `VetRepository`.
- `model/` — shared mapped superclasses: `BaseEntity` (JPA id + `isNew()`), `NamedEntity` (adds
  `name`), `Person` (adds validated `firstName`/`lastName`). Entities extend these.
- `system/` — cross-cutting config: `CacheConfiguration` (JCache/Caffeine for vets),
  `WebConfiguration`, `WelcomeController`, and `CrashController` (deliberate error endpoint for the
  error-page demo).
- `PetClinicApplication` is the `main` entry point. `PetClinicRuntimeHints` registers
  GraalVM native-image reflection/resource hints (including the nested `db/{h2,mysql,postgres}/`
  SQL scripts) — update it when adding resources that native builds must see.

Key patterns to follow when extending:

- **Repositories** are Spring Data interfaces (`extends JpaRepository` / `Repository`); no service
  layer — controllers call repositories directly.
- **Controllers** use `@ModelAttribute` factory methods to load/instantiate the form-backing entity
  (e.g. `OwnerController.findOwner`), `@InitBinder` to disallow binding `id`, and `@Valid` +
  `BindingResult` for form validation. They return Thymeleaf view names or `redirect:` strings and
  pass user messages via `RedirectAttributes` flash attributes.
- **Validation** combines Jakarta annotations on entities (`@NotBlank`, `@Pattern`, custom `@Size`,
  future-date checks on `Visit`) with the programmatic `PetValidator`. `PetTypeFormatter` converts
  between `PetType` and its string form in web binding.
- **i18n**: user-facing strings live in `messages/messages*.properties`. `I18nPropertiesSyncTest`
  enforces that all locale files have the same keys — add new keys to every locale file or that
  test fails.

## Tests

- Web-layer tests use `@WebMvcTest` slices (e.g. `OwnerControllerTests`) with mocked repositories.
- `ClinicServiceTests` is a `@DataJpaTest`-style integration test against the repositories.
- Dual-purpose DB apps in `src/test/java`: `PetClinicIntegrationTests`, `MysqlTestApplication`,
  `PostgresIntegrationTests` each have a `main()` for running the app in your IDE against that DB,
  and also run as integration tests. MySQL/Postgres tests use Testcontainers / Docker Compose, so
  a Docker daemon is required for those.
