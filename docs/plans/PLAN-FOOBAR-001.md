## Phase 1: Persistence
SKIP — The endpoint is purely in-memory with no database interaction, entities, repositories, or schema migrations required.

## Phase 2: Integration Clients
SKIP — No external services, REST clients, or third-party adapters are involved.

## Phase 3: Domain Logic
SKIP — There is no business logic, service layer, or domain model needed. The response is a static, hardcoded value. The `HelloResponse` record serves only as a serialization vehicle and is defined inline within the controller; it does not represent domain state.

## Phase 4: REST API

### 4.1 Production Code

**New file:** `src/main/java/org/springframework/samples/petclinic/system/HelloController.java`

- Declare the class in package `org.springframework.samples.petclinic.system` to sit alongside the existing `WelcomeController` and `CrashController`.
- Annotate the class with `@RestController` (implies `@ResponseBody` on all handler methods; signals JSON-only intent and matches the pattern established by other controllers in the project).
- Define a package-private nested `record HelloResponse(String message)` as the response DTO. A Java record is appropriate here: zero boilerplate, immutable, and Jackson (already on the classpath via `spring-boot-starter-web`) serializes it automatically via its accessor methods.
- Define a single handler method `HelloResponse hello()` annotated with `@GetMapping("/hello")`. The method returns `new HelloResponse("hello, this is for testing purpose")`. Spring MVC will serialize this to `{"message":"hello, this is for testing purpose"}` and set `Content-Type: application/json` automatically.
- Do **not** annotate with `@RequestMapping` at the class level; keep it minimal.
- The 405 Method Not Allowed behaviour for POST/PUT/DELETE is provided automatically by Spring MVC when no mapping for those methods exists on `/hello` — no explicit configuration needed.
- No new Maven or Gradle dependencies are introduced; everything relies on `spring-boot-starter-web` and its transitive Jackson dependency already present in the POM and build files.

### 4.2 Test Code

**New file:** `src/test/java/org/springframework/samples/petclinic/system/HelloControllerTest.java`

- Place the test in package `org.springframework.samples.petclinic.system` to mirror the production class location.
- Annotate the test class with `@WebMvcTest(HelloController.class)`. This loads only the web layer slice for `HelloController`, keeping the test fast and free of database or full-context dependencies.
- Inject `MockMvc` via `@Autowired`.
- **Test case 1 — happy path:** Perform `mockMvc.perform(get("/hello"))` and chain the following assertions:
  - `.andExpect(status().isOk())` — HTTP 200.
  - `.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))` — `Content-Type: application/json`.
  - `.andExpect(content().json("{\"message\":\"hello, this is for testing purpose\"}"))` — exact JSON body match using MockMvc's `content().json(...)` which does a semantic JSON equality check (strict by passing `true` as the second argument to ensure no extra fields are tolerated).
- **Test case 2 — POST returns 405:** Perform `mockMvc.perform(post("/hello"))` and assert `.andExpect(status().isMethodNotAllowed())`.
- **Test case 3 — PUT returns 405:** Perform `mockMvc.perform(put("/hello"))` and assert `.andExpect(status().isMethodNotAllowed())`.
- **Test case 4 — DELETE returns 405:** Perform `mockMvc.perform(delete("/hello"))` and assert `.andExpect(status().isMethodNotAllowed())`.
- Import static `org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*` and `org.springframework.test.web.servlet.result.MockMvcResultMatchers.*` for readability.
- No mocking of collaborators is needed (`@MockitoBean` etc.) because the controller has no dependencies.

### 4.3 Verification Checklist
- Run the full test suite (`./mvnw verify` or `./gradlew test`) to confirm no pre-existing tests regress; the new `@WebMvcTest` slice is isolated and will not affect other slices.
- Confirm no `pom.xml` or `build.gradle` / `build.gradle.kts` files were modified (dependency-free requirement).

---

## Summary
Two files are added: `HelloController.java` in the `system` package (a `@RestController` with a single `@GetMapping("/hello")` that returns a static JSON record), and `HelloControllerTest.java` (a `@WebMvcTest` slice test asserting the 200 status, exact JSON body, correct `Content-Type`, and 405 responses for mutating methods). No dependencies, persistence, or business logic are introduced.