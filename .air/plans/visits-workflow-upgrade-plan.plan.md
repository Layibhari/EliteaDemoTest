## 1. Goal
Add a proper visits workflow to PetClinic so users can browse, filter, and edit visit records per pet without leaving the existing owner/pet navigation model.

## 2. Approach
The current visit implementation is intentionally thin: `VisitController` only supports creating a new visit and it mutates the loaded pet inside `@ModelAttribute` (`src/main/java/org/springframework/samples/petclinic/owner/VisitController.java:62-102`), while the only visit display is the embedded table in `src/main/resources/templates/owners/ownerDetails.html:44-79`. The cleanest improvement is to keep the existing Spring MVC package structure, introduce visit-specific query access through a dedicated repository, and refactor visit loading so new, list, and edit flows are explicit rather than hidden inside binder side effects.

## 3. File Changes
- **Modify** `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java`  
  Replace the side-effecting `@ModelAttribute` flow at `:62-79` with explicit owner/pet/visit lookup helpers, keep the existing new-visit flow, and add visit history plus visit edit endpoints.
- **Modify** `src/main/java/org/springframework/samples/petclinic/owner/Visit.java`  
  Extend the entity defined at `:34-66` with a `Pet` association so visits can be queried directly and updated safely by id within owner/pet scope.
- **Modify** `src/main/java/org/springframework/samples/petclinic/owner/Pet.java`  
  Update the visit mapping at `:56-83` to a bidirectional association and ensure `addVisit()` sets the parent pet reference.
- **Create** `src/main/java/org/springframework/samples/petclinic/owner/VisitRepository.java`  
  Add Spring Data JPA queries for paginated visit history by owner+pet, filtered by date range and optional description text, plus scoped lookup for edit routes.
- **Modify** `src/main/resources/templates/pets/createOrUpdateVisitForm.html`  
  Reuse the current form (`:7-55`) for both create and edit states, add localized headings/button text, and show prior visits consistently instead of only when `visit['new']` is false.
- **Create** `src/main/resources/templates/pets/visitsList.html`  
  Add a dedicated Thymeleaf screen for paginated visit history with filter inputs, clear navigation back to the owner and pet, and edit links per visit.
- **Modify** `src/main/resources/templates/owners/ownerDetails.html`  
  Enhance the pets/visits section at `:44-79` with a link from each pet to its full visit history page and, if needed, a tighter recent-visits summary.
- **Modify** `src/main/resources/messages/messages.properties`  
  Add keys for visit history, filter labels, empty-state text, edit actions, and success messages.
- **Modify** `src/main/resources/messages/messages_de.properties`
- **Modify** `src/main/resources/messages/messages_es.properties`
- **Modify** `src/main/resources/messages/messages_fa.properties`
- **Modify** `src/main/resources/messages/messages_ko.properties`
- **Modify** `src/main/resources/messages/messages_pt.properties`
- **Modify** `src/main/resources/messages/messages_ru.properties`
- **Modify** `src/main/resources/messages/messages_tr.properties`  
  Keep locale keys in sync with the base bundle so `I18nPropertiesSyncTest` continues to pass.
- **Modify** `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java`  
  Expand the current controller coverage (`:67-92`) to cover list, filter, edit, and scoped-not-found behavior.
- **Modify** `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java`  
  Update owner-detail expectations around visit links and any adjusted visit presentation near `:216-228`.
- **Create** `src/test/java/org/springframework/samples/petclinic/owner/VisitRepositoryTests.java`  
  Add focused persistence tests for history ordering, owner/pet scoping, and filter behavior.

## 4. Implementation Steps
### Task 1: Make visits queryable as first-class records
1. In `src/main/java/org/springframework/samples/petclinic/owner/Visit.java`, add a `@ManyToOne` association to `Pet` and getters/setters so a visit knows its parent pet.
2. In `src/main/java/org/springframework/samples/petclinic/owner/Pet.java`, convert the current `@OneToMany` mapping at `:56-59` to the bidirectional form and update `addVisit()` at `:81-83` to set `visit.setPet(this)` before inserting into the collection.
3. Create `src/main/java/org/springframework/samples/petclinic/owner/VisitRepository.java` with query methods such as paginated history by `pet.id` and `pet.owner_id` scope, plus a repository method that resolves a visit by `visitId`, `petId`, and `ownerId` together.

### Task 2: Refactor the controller away from side-effect loading
1. In `src/main/java/org/springframework/samples/petclinic/owner/VisitController.java`, replace the current `@ModelAttribute("visit")` implementation at `:62-79` with explicit helpers that load `Owner`, `Pet`, and `Visit` without mutating the collection during every request.
2. Keep `/owners/{ownerId}/pets/{petId}/visits/new` as the entry point for creation, but instantiate a new `Visit` only for the new form handler and POST path.
3. Add `GET /owners/{ownerId}/pets/{petId}/visits` for paginated visit history and filtering, and `GET/POST /owners/{ownerId}/pets/{petId}/visits/{visitId}/edit` for updating a visit in place.
4. Continue redirecting through owner-scoped URLs so the workflow stays aligned with the existing navigation conventions already used in `OwnerController` and `PetController`.

### Task 3: Build the visit history UI
1. In `src/main/resources/templates/pets/createOrUpdateVisitForm.html`, make the heading, submit button, and history table support both create and edit mode.
2. Create `src/main/resources/templates/pets/visitsList.html` with a filter form for description/date range, a paginated results table, and links back to the owner details page and the add-visit form.
3. In `src/main/resources/templates/owners/ownerDetails.html`, add a per-pet `View Visit History` action next to the existing `Edit Pet` and `Add Visit` links so the new page is discoverable from the main workflow.

### Task 4: Localize new UI text
1. Add new message keys to `src/main/resources/messages/messages.properties` for labels, filter placeholders, headings, empty-state copy, and success messages used by the new views and controller flash attributes.
2. Mirror the same keys in every localized bundle under `src/main/resources/messages/` so the repository’s translation sync test remains valid.

### Task 5: Add focused test coverage
1. Extend `src/test/java/org/springframework/samples/petclinic/owner/VisitControllerTests.java` with cases for: history page render, valid filter parameters, edit form render, successful edit POST, invalid edit POST, and rejected access when the visit does not belong to the owner/pet in the URL.
2. Update `src/test/java/org/springframework/samples/petclinic/owner/OwnerControllerTests.java` so owner details still render visits correctly and expose the new visit-history action.
3. Add `src/test/java/org/springframework/samples/petclinic/owner/VisitRepositoryTests.java` to verify descending date ordering, owner/pet scoping, and description/date filtering against seeded test data.
4. Keep `src/test/java/org/springframework/samples/petclinic/system/I18nPropertiesSyncTest.java` green by ensuring no hard-coded strings are introduced in new HTML.

## 5. Acceptance Criteria
- `GET /owners/{ownerId}/pets/{petId}/visits` returns HTTP 200 and renders a dedicated visit history page for that pet.
- Visit history is paginated and sorted by visit date descending on the history page.
- Users can filter visit history by at least description text and date range; the filtered page preserves owner/pet scope.
- `GET /owners/{ownerId}/pets/{petId}/visits/{visitId}/edit` loads an edit form for a visit that belongs to that pet and owner.
- `POST /owners/{ownerId}/pets/{petId}/visits/{visitId}/edit` persists updated visit date/description and redirects back to the visit history or owner details page with a success flash message.
- Requests for a visit id outside the current owner/pet scope do not update data and return a safe failure path (4xx or mapped exception handling consistent with current controller behavior).
- The existing add-visit flow at `/owners/{ownerId}/pets/{petId}/visits/new` continues to work.
- Each pet block on the owner details page exposes a navigation action to the full visit history page.
- All new user-visible strings are backed by message bundle keys, and all locale property files remain key-synchronized.
- Targeted controller and repository tests cover the new routes and filtering behavior.

## 6. Verification Steps
1. Run targeted tests: `./mvnw -Dtest=VisitControllerTests,OwnerControllerTests,VisitRepositoryTests,I18nPropertiesSyncTest test`
2. Run the broader suite: `./mvnw test`
3. Start the app: `./mvnw spring-boot:run`
4. Manual verification:
   - Open an owner with pets and confirm each pet has `Add Visit` and `View Visit History` actions.
   - Add a new visit and verify it appears on both the owner details page and the dedicated history page.
   - Edit an existing visit and verify the updated description/date is reflected immediately.
   - Apply description/date filters and verify pagination and results stay scoped to the selected pet.
   - Switch locale using the existing message bundles and confirm no raw hard-coded labels appear in the new screens.

## 7. Risks & Mitigations
- **Risk:** Changing the `Pet` ↔ `Visit` mapping can break cascade persistence or duplicate visit rows.  
  **Mitigation:** Keep ownership on `Visit`, use a single helper path (`Pet.addVisit`) to set both sides, and cover persistence behavior in `VisitRepositoryTests`.
- **Risk:** Edit routes could accidentally expose visits from another pet or owner if lookup is only by `visitId`.  
  **Mitigation:** Use repository methods scoped by `ownerId`, `petId`, and `visitId` together and add negative WebMvc tests.
- **Risk:** New message keys can fail CI because `I18nPropertiesSyncTest` enforces full bundle parity.  
  **Mitigation:** Add every new key to all locale files in the same change, even if non-English values initially mirror the base text.
- **Risk:** The current controller relies on `OwnerRepository` only; adding a broader workflow can sprawl if query responsibilities stay mixed.  
  **Mitigation:** Keep owner/pet loading in the controller but move visit-history retrieval into `VisitRepository` so the change stays localized to the `owner` package.

## Scope Note
This plan is intentionally limited to the visits workflow. The repository also has separate improvement opportunities in owner search/discovery and the vet directory, but those should be handled as separate plans because they touch different UI paths and query patterns.