Modernization Plan — Java Upgrade & Cloud Readiness (plan-20260722-01)

Scope
- Assessment report used: .github/modernize/assessment/reports/report-20260722074700/report.json
- Domains: java-upgrade, cloud-readiness (security domain excluded per request)
- Workspace: spring-petclinic

Executive summary (3–6 bullets)
- Resolve mandatory cloud-readiness issues so the app can run reliably as multiple instances in Azure: externalize in-process cache to a managed distributed cache (Azure Cache for Redis) and remove stateful defaults in production.
- Replace hardcoded/local defaults (JDBC endpoints, test endpoints) and move secrets to Azure Key Vault so CI/CD and staging environments can run safely and reproducibly.
- Containerize, add a CI build/push to ACR, and deploy a staging instance (Container Apps or App Service) for smoke tests; use Testcontainers / env-driven tests to decouple CI from local infra.
- Short-term practical changes (cache, test parameterization, secrets) reduce blocking risks; medium-term work (DB migration, jakarta alignment) requires planning and data-migration steps.
- Total estimated effort (this plan): 173 story points (see tasks breakdown). Branch: modernize/java-cloud-readiness/20260722-01

Files produced
- plan: .github/modernize/plans/plan-20260722-01/plan.md
- tasks: .github/modernize/plans/plan-20260722-01/tasks.json

Branch suggestion
- Create a feature branch for all changes: modernize/java-cloud-readiness/20260722-01

Top-level prioritized tasks (IDs, ordering, and dependencies)
1. 01-externalize-cache-redis — Externalize cache to Azure Cache for Redis (est. 35 SP) — dependencies: none
2. 02-db-parameterize-keyvault — Parameterize DB + migrate secrets to Azure Key Vault; guidance for Azure DB for MySQL/Postgres + migration approach (est. 40 SP) — dependencies: none
3. 03-update-tests — Update integration & CI tests (Testcontainers or env-driven endpoints); remove hardcoded localhost (est. 25 SP) — dependencies: 01, 02
4. 04-containerize-ci-deploy — Add Dockerfile, build image, push to ACR, CI job; deploy to staging and smoke test (est. 30 SP) — dependencies: 03
5. 05-jakarta-alignment — Align javax → jakarta packages and resolve mixed dependencies (est. 30 SP) — dependencies: 03, 04
6. 06-observability-probes — Add Spring Actuator, Application Insights wiring, and Kubernetes health/readiness probes (est. 13 SP) — dependencies: 04

Per-task detail

1) 01-externalize-cache-redis (35 SP) — Risk: Medium
Description
Replace the embedded JCache/Caffeine configuration with a Spring Cache abstraction backed by Azure Cache for Redis for production. Preserve a local Caffeine fallback for developer profiles.
Acceptance criteria
- Production profile uses RedisCacheManager and connects to an Azure Cache for Redis instance via environment variables.
- Local dev profile continues to use Caffeine without changing developer workflows.
- Unit tests and a simple integration test that exercise cache expiry & cache hit behaviour pass.
Implementation steps (concrete)
- Step A: Add Redis dependency (spring-boot-starter-data-redis) and a RedisCacheManager bean in CacheConfiguration with @Profile("!local") (or spring.profiles.active) and keep existing Caffeine configuration under @Profile("local" or "dev").
- Step B: Add application-redis.properties / application-production.properties entries that read REDIS_HOST/REDIS_PORT/REDIS_PASSWORD from env and document how to set them in CI and the staging deployment; provide a small sample ARM/Bicep/az cli snippet in docs to provision Azure Cache for Redis for staging.
Files & configs likely to change
- src/main/java/org/springframework/samples/petclinic/system/CacheConfiguration.java
- src/main/java/.../vet/VetRepository.java (annotations usage review)
- build.gradle and/or pom.xml (add spring-data-redis)
- src/main/resources/application-*.properties (application-production.properties, application-local.properties)
Minimal test to validate success
- Run a short integration test that boots the app with production profile config (pointing to a test Redis instance or Testcontainers redis) and asserts that a cached method returns cached values across requests; for local dev profile assert Caffeine still used.
Notes
- Use Spring profiles to avoid switching runtime behaviour by code changes. In CI, use an ephemeral Redis instance (Testcontainers) or an Azure staging Redis instance.


2) 02-db-parameterize-keyvault (40 SP) — Risk: High (data migration + secrets)
Description
Parameterize DB connection details (host, port, username, database, SSL mode) in Spring properties and remove hardcoded localhost fallbacks in production profiles. Remove credentials from properties; integrate Azure Key Vault as the secret store for production. Provide guidance for provisioning Azure Database for MySQL/Postgres and a migration approach (Azure Database Migration Service) with minimal downtime.
Acceptance criteria
- No plaintext credentials remain in committed property files for production profiles.
- App can be configured via environment variables or Key Vault references (managed identity or client credentials) to connect to Azure DB.
- Integration test(s) can run against a Testcontainer DB using env overrides.
Implementation steps (concrete)
- Step A: Refactor application-*properties to read DB URL, username, and password from env variables (SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD) and remove localhost default values in production profile files.
- Step B: Add Key Vault configuration pattern (Azure Key Vault provider for Spring Boot or use managed identity), document steps: provision Azure DB for MySQL/Postgres, create firewall rules, run DMS for data migration (or export/import for dev), then switch connection strings using Key Vault references or env secrets in CI/CD.
Files & configs likely to change
- src/main/resources/application-mysql.properties
- src/main/resources/application-postgres.properties
- src/main/resources/application-production.properties
- pom.xml or build.gradle (if adding azure-spring-boot-starter or azure-keyvault-secrets dependency)
- CI pipeline definitions (.github/workflows/*) to read secrets from Key Vault / GitHub Secrets
Minimal test to validate success
- Run Postgres/MySQL Testcontainer in CI with SPRING_DATASOURCE_* env vars; execute integration test suite and verify DB connectivity and successful migrations (schema apply). If using Key Vault references, test reading a secret from Key Vault via a short smoke test using a test keyvault or mock.
Guidance for Azure DB migration
- Recommend Azure Database Migration Service (DMS) for online minimal-downtime migrations; for small datasets, a dump/restore may suffice. Document pre-checks: character sets, extensions, connection strings, and required firewall rules. Plan a maintenance window and practice migration on a staging copy before production cutover.


3) 03-update-tests (25 SP) — Risk: Medium-Low
Description
Replace hardcoded localhost endpoints and plaintext HTTP assumptions in integration tests. Prefer Testcontainers for DB and external dependencies, or make tests read endpoints from environment variables so CI can run against staging/ephemeral endpoints.
Acceptance criteria
- No integration test hardcodes http://localhost:... or jdbc:localhost in code used by CI/staging.
- CI integration job can run tests against Testcontainers DB and Testcontainers Redis (or env-provided services) and passes.
Implementation steps (concrete)
- Step A: Replace hardcoded URLs in src/test/java/**/*.java with usage of a shared TestProperties or environment-driven helper (System.getenv("TEST_BASE_URL") or use @DynamicPropertySource with Testcontainers).
- Step B: Add Testcontainers-based startup for DB and optional Redis in integration test suite; update CI to run integration tests in a separate job that waits for container readiness.
Files & configs likely to change
- src/test/java/org/springframework/samples/petclinic/*IntegrationTests.java
- build.gradle / pom.xml (add Testcontainers dependencies: testcontainers, testcontainers-postgres/redis)
- .github/workflows/* (CI job to run integration tests in dedicated job)
Minimal test to validate success
- CI job that runs only integration tests using Testcontainers and passes. Locally, running mvn -Pintegration or ./gradlew integrationTest should start containers and complete tests.


4) 04-containerize-ci-deploy (30 SP) — Risk: Medium
Description
Add a Dockerfile, build an image in CI, push to Azure Container Registry (ACR), and add a simple staging deploy (Container Apps or App Service). Add a smoke test that hits /actuator/health and a key application endpoint.
Acceptance criteria
- CI builds an image and pushes to ACR on the feature branch or a PR merge to a staging branch.
- Staging deployment receives the new image and passes a smoke test hitting /actuator/health and a simple endpoint (e.g., GET /vets).
Implementation steps (concrete)
- Step A: Add a multi-stage Dockerfile that builds jar and produces a lean runtime image; update build pipeline (.github/workflows/ci.yml) to build and push image to ACR when ACR creds are available.
- Step B: Add a staging deploy job using Azure CLI or GitHub Actions azure/container-apps-deploy or webapps-deploy for App Service. Add a smoke-test job that calls /actuator/health and a sample API endpoint.
Files & configs likely to change
- Dockerfile (root)
- build.gradle / pom.xml (ensure app artifact is buildable in container)
- .github/workflows/ci.yml (or create .github/workflows/containerize-and-deploy.yml)
- infra snippets (optional): ./deploy/azure-containerapp-staging.yml or az cli scripts
Minimal test to validate success
- CI log showing successful build/push to ACR and smoke-test step returning HTTP 200 for /actuator/health and sample endpoint.
Rollout/rollback notes
- Use image tags (commit SHA) in staging; to rollback, redeploy previous image tag; for production use deployment strategy (blue/green) supported by target service.


5) 05-jakarta-alignment (30 SP) — Risk: High
Description
Address mixed javax/jakarta usage: standardize on jakarta.* APIs for Spring Boot 4.1.0, update dependencies, adjust imports, and run automated compatibility checks. This is typically the highest-risk code change because it touches APIs across the app and third-party libs.
Acceptance criteria
- Build succeeds and unit tests pass with jakarta-aligned dependencies.
- No runtime ClassNotFoundException or NoSuchMethodError related to javax/jakarta APIs in CI integration tests.
Implementation steps (concrete)
- Step A: Inventory javax vs jakarta usage via search (grep) and update dependencies to jakarta-compatible versions (check pom.xml and build.gradle for javax-cache, jakarta.xml.bind-api, etc.).
- Step B: For source code, run a codemod or IDE refactor to change imports where necessary and replace incompatible libraries; run full test suite and evaluate runtime behaviour.
Files & configs likely to change
- pom.xml and/or build.gradle (versions of dependencies)
- src/main/java/**/* (imports referencing javax.*)
- src/main/resources (any XML or config referencing javax namespaces)
Minimal test to validate success
- Full build + unit + integration test run in CI; specifically exercise areas that use javax.* APIs (JAXB, caching) in integration tests.
Notes
- Consider breaking this into smaller pull requests per package or module to limit blast radius. Keep a rollback plan (revert PR) ready.


6) 06-observability-probes (13 SP) — Risk: Low-Medium
Description
Add Spring Boot Actuator, configure Application Insights telemetry exporter (or azure-spring-boot-starter), and add Kubernetes liveness/readiness probes (if deploying to AKS or Container Apps) or Application Insights connection for App Service.
Acceptance criteria
- /actuator/health is available and reports readiness/liveness.
- Telemetry is successfully sent to Application Insights in staging (verify in portal or CI logs with a test event).
- Container health probes configured in deployment manifest or app settings.
Implementation steps (concrete)
- Step A: Add spring-boot-starter-actuator and configure management.endpoints.web.exposure.include=health,info,prometheus; add application-insights starter or configure OTLP exporter for App Insights.
- Step B: Add Kubernetes/container-apps probe configuration in deployment manifest or pipeline step (readinessProbe: HTTP GET /actuator/health/liveness and appropriate timeouts).
Files & configs likely to change
- pom.xml / build.gradle (add actuator and appinsights dependencies)
- src/main/resources/application-*.properties (actuator configuration, applicationinsights key via env)
- deployment manifest / .github/workflows/* deploy steps
Minimal test to validate success
- After staging deploy, call /actuator/health and verify 200 and telemetry events received in Application Insights.


Overall estimated effort
- Total (sum of tasks above): 173 story points

Phased schedule (3 phases)
- Quick wins (0–3 weeks):
  * 01-externalize-cache-redis (35 SP) — mandatory for multi-instance reliability
  * 03-update-tests (25 SP) — unblocks CI and safe deployments
  * 06-observability-probes (13 SP) — improves ops visibility
  Estimated quick-wins subtotal: 73 SP

- Medium (3–8 weeks):
  * 02-db-parameterize-keyvault (40 SP) — requires provisioning and migration planning
  * 04-containerize-ci-deploy (30 SP) — build/push/deploy to staging + smoke test
  Estimated medium subtotal: 70 SP

- Long-term (8+ weeks):
  * 05-jakarta-alignment (30 SP) — high-risk, larger refactor; schedule into its own milestone and break into smaller PRs
  Estimated long-term subtotal: 30 SP

Acceptance gates / quality gates
- Gate 1 (post-quick-wins): CI build passing, integration tests run via Testcontainers in CI, smoke test against staging passes.
- Gate 2 (post-medium): Staging runs with Redis and Azure DB connectivity (or equivalent Testcontainers-based integration) and performance/scale smoke tests validate multi-instance behavior.
- Gate 3 (post-long-term): Full regression test and runtime verification; production rollout only after successful performance test and a dry-run rollback procedure.

PR checklist (must be completed before merge)
- [ ] Branch created from modernize/java-cloud-readiness/20260722-01
- [ ] All unit tests pass locally and in CI
- [ ] Integration tests passing in CI (Testcontainers or staging-run)
- [ ] No plaintext secrets or credentials in commits or properties
- [ ] Documentation updated (README/DEPLOYMENT.md) with runbook to provision Redis, Azure DB, Key Vault and deployment steps
- [ ] Smoke-test steps added to CI pipeline and verified on staging
- [ ] Rollback steps documented (previous image tag, revert PR)
- [ ] Feature toggles / Spring profiles verified (local vs production)

Rollout & rollback notes
- Deploy to a dedicated staging environment first (use ACR image tags tied to commits). If smoke tests pass, promote image to production via tag promotion or use blue/green deployment.
- For cache switch: introduce Redis behind a Spring profile; keep Caffeine active for local dev. To rollback from Redis issues, flip profile/environment to use local cache image or redeploy previous image.
- For DB migration: do not cutover production until migration rehearsals complete. Use DMS for online migration where possible and have a fallback plan (read-only mode and revert DNS/connection string to old DB) before changing writes.

Notes and references
- Assessment artifacts used: .github/modernize/assessment/reports/report-20260722074700/report.json
- Azure guidance: Azure Cache for Redis, Azure Database for MySQL/Postgres, Azure Key Vault, Azure Database Migration Service

Next steps (recommended immediate actions)
1. Create the branch: git checkout -b modernize/java-cloud-readiness/20260722-01
2. Create a minimal PR that: (a) adds Redis dependency + CacheConfiguration profile for local vs production (small, testable), (b) updates README with how to run with Redis Testcontainer — this PR should be small to establish the pattern.
3. Parallel work: begin DB parameterization changes in a separate branch (or the same feature branch as small, focused commits) and prepare infra (ACR/Redis/KeyVault) for staging.

Plan author: planning-coordinator
Generated: 2026-07-22T08:03:01+02:00
