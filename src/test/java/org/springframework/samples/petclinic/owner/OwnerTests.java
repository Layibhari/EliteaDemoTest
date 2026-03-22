/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Owner} domain logic.
 * <p>
 * These are pure unit tests with no Spring context, covering the non-trivial lookup and
 * mutation methods on the Owner aggregate root.
 */
class OwnerTests {

	private Owner owner;

	private Pet savedPet;

	private Pet newPet;

	@BeforeEach
	void setUp() {
		owner = new Owner();
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		savedPet = new Pet();
		savedPet.setName("Max");
		savedPet.setId(1);
		// addPet won't add a pet that already has an id, so add directly
		owner.getPets().add(savedPet);

		newPet = new Pet();
		newPet.setName("Lucky");
		// newPet has no id set, so isNew() == true
	}

	@Nested
	class AddPet {

		@Test
		void shouldAddNewPet() {
			owner.addPet(newPet);
			assertThat(owner.getPets()).contains(newPet);
		}

		@Test
		void shouldNotAddPetWithExistingId() {
			Pet alreadySaved = new Pet();
			alreadySaved.setName("Buddy");
			alreadySaved.setId(2);

			int sizeBefore = owner.getPets().size();
			owner.addPet(alreadySaved);
			assertThat(owner.getPets()).hasSize(sizeBefore);
		}

	}

	@Nested
	class GetPetByName {

		@Test
		void shouldFindPetByExactName() {
			assertThat(owner.getPet("Max")).isSameAs(savedPet);
		}

		@Test
		void shouldFindPetByNameCaseInsensitive() {
			assertThat(owner.getPet("max")).isSameAs(savedPet);
			assertThat(owner.getPet("MAX")).isSameAs(savedPet);
		}

		@Test
		void shouldReturnNullForUnknownName() {
			assertThat(owner.getPet("Unknown")).isNull();
		}

		@Test
		void shouldHandlePetWithNullName() {
			Pet nullNamePet = new Pet();
			// name is null by default
			owner.getPets().add(nullNamePet);

			// Should not match and should not throw NPE
			assertThat(owner.getPet("Max")).isSameAs(savedPet);
			assertThat(owner.getPet("anything")).isNull();
		}

	}

	@Nested
	class GetPetById {

		@Test
		void shouldFindPetById() {
			assertThat(owner.getPet(Integer.valueOf(1))).isSameAs(savedPet);
		}

		@Test
		void shouldReturnNullForUnknownId() {
			assertThat(owner.getPet(Integer.valueOf(999))).isNull();
		}

		@Test
		void shouldSkipNewPetsWhenSearchingById() {
			owner.addPet(newPet);
			// newPet has no id, so it should be skipped
			assertThat(owner.getPet(Integer.valueOf(1))).isSameAs(savedPet);
		}

	}

	@Nested
	class GetPetByNameWithIgnoreNew {

		@Test
		void shouldIgnoreNewPetsWhenFlagIsTrue() {
			owner.addPet(newPet);
			// newPet is "Lucky" and isNew() == true
			assertThat(owner.getPet("Lucky", true)).isNull();
		}

		@Test
		void shouldIncludeNewPetsWhenFlagIsFalse() {
			owner.addPet(newPet);
			assertThat(owner.getPet("Lucky", false)).isSameAs(newPet);
		}

	}

	@Nested
	class ToString {

		@Test
		void shouldIncludeOwnerFields() {
			owner.setId(42);
			owner.setAddress("110 W. Liberty St.");
			owner.setCity("Madison");
			owner.setTelephone("6085551023");

			String result = owner.toString();

			assertThat(result).contains("firstName = 'George'");
			assertThat(result).contains("lastName = 'Franklin'");
			assertThat(result).contains("address = '110 W. Liberty St.'");
			assertThat(result).contains("city = 'Madison'");
			assertThat(result).contains("telephone = '6085551023'");
		}

		@Test
		void shouldIndicateNewOwner() {
			// owner has no id set, so isNew() == true
			String result = owner.toString();
			assertThat(result).contains("new = true");
		}

		@Test
		void shouldIndicateExistingOwner() {
			owner.setId(1);
			String result = owner.toString();
			assertThat(result).contains("new = false");
		}

	}

	@Nested
	class AddVisit {

		@Test
		void shouldAddVisitToExistingPet() {
			Visit visit = new Visit();
			visit.setDescription("checkup");

			owner.addVisit(savedPet.getId(), visit);

			assertThat(savedPet.getVisits()).contains(visit);
		}

		@Test
		void shouldThrowWhenPetIdIsNull() {
			Visit visit = new Visit();
			visit.setDescription("checkup");

			assertThatThrownBy(() -> owner.addVisit(null, visit)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Pet identifier must not be null");
		}

		@Test
		void shouldThrowWhenVisitIsNull() {
			assertThatThrownBy(() -> owner.addVisit(savedPet.getId(), null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Visit must not be null");
		}

		@Test
		void shouldThrowWhenPetNotFound() {
			Visit visit = new Visit();
			visit.setDescription("checkup");

			assertThatThrownBy(() -> owner.addVisit(999, visit)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid Pet identifier");
		}

	}

}
