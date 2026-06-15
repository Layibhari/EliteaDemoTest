# Owner Backend Automation PR Split Plan

Target repository: spring-projects/spring-petclinic
Fork owner: ashik-shaji
Base branch: main

This split keeps test files intact and groups review by test layer/sub-module. The Owner automation set contains 24 reviewed valid scenarios.

## PR 1: Foundation - Reviewed Owner Automation Scenarios

Test count: 0
Scope: scenario and review artifacts only
Files:
- docs/test-automation/owner-scenarios.json
- docs/test-automation/owner-scenarios.md
- docs/test-automation/owner-scenarios.xlsx
- docs/test-automation/owner-scenarios-reviewed.md
- docs/test-automation/owner-valid-scenarios.json
- docs/test-automation/owner-valid-scenarios.md
- docs/test-automation/owner-valid-scenarios.xlsx
- docs/test-automation/owner-pr-split-plan.md

Purpose:
- Documents the reviewed workbook import.
- Records the valid-only scenarios used for automation.
- Provides the PR split map without adding executable tests.

## PR 2: Owner API E2E Automation

Test count: 9
Scope: real HTTP Owner workflows through the running Spring Boot application
Files:
- src/test/java/org/springframework/samples/petclinic/owner/e2e/OwnerHttpE2ETests.java

Scenario IDs:
- E2E-001
- E2E-003
- E2E-004
- E2E-005
- E2E-006
- E2E-007
- E2E-008
- E2E-009
- E2E-010

## PR 3: Owner Integration Automation

Test count: 7
Scope: repository, Bean Validation, and controller binding integration checks
Files:
- src/test/java/org/springframework/samples/petclinic/owner/integration/OwnerRepositoryIntegrationTests.java
- src/test/java/org/springframework/samples/petclinic/owner/integration/OwnerValidationIntegrationTests.java
- src/test/java/org/springframework/samples/petclinic/owner/integration/OwnerControllerBindingTests.java

Scenario IDs:
- INT-002
- INT-003
- INT-004
- INT-005
- INT-006
- INT-007
- INT-008

Exception note:
- This PR has 7 tests instead of 8-10 to keep the integration files intact and avoid mixing unit tests into the integration review surface.

## PR 4: Owner Unit Automation

Test count: 8
Scope: Owner domain helper and telephone validation unit coverage
Files:
- src/test/java/org/springframework/samples/petclinic/owner/unit/OwnerTests.java
- src/test/java/org/springframework/samples/petclinic/owner/unit/OwnerValidationTests.java

Scenario IDs:
- UNIT-001
- UNIT-002
- UNIT-003
- UNIT-004
- UNIT-005
- UNIT-006
- UNIT-007
- UNIT-008

## Merge-Union Verification

Before opening PRs:
- Create one combined automation branch.
- Create each split branch from main.
- Merge all split branches into a temporary verification branch.
- Compare the merged result with the combined automation branch.
- Run focused Owner automation, all applicable backend tests, and report generation from the merged result.
