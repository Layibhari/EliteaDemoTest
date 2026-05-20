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

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Owner} domain object methods.
 */
class OwnerTests {

	private Owner owner;

	private Pet savedPet;

	private Pet newPet;

	@BeforeEach
	void setUp() {
		owner = new Owner();

		savedPet = new Pet();
		savedPet.setId(10);
		savedPet.setName("Buddy");
		PetType dog = new PetType();
		dog.setName("dog");
		savedPet.setType(dog);
		savedPet.setBirthDate(LocalDate.of(2020, 1, 1));

		newPet = new Pet();
		newPet.setName("Luna");
	}

	@Nested
	class AddPet {

		@Test
		void addsNewPetToOwner() {
			owner.addPet(newPet);
			assertThat(owner.getPets()).contains(newPet);
		}

		@Test
		void doesNotAddExistingPetAgain() {
			int sizeBefore = owner.getPets().size();
			owner.addPet(savedPet);
			assertThat(owner.getPets()).hasSize(sizeBefore);
		}

	}

	@Nested
	class GetPetByName {

		@BeforeEach
		void addSavedPet() {
			owner.getPets().add(savedPet);
		}

		@Test
		void returnsPetByNameCaseInsensitive() {
			assertThat(owner.getPet("buddy")).isEqualTo(savedPet);
			assertThat(owner.getPet("BUDDY")).isEqualTo(savedPet);
			assertThat(owner.getPet("Buddy")).isEqualTo(savedPet);
		}

		@Test
		void returnsNullWhenNameNotFound() {
			assertThat(owner.getPet("Ghost")).isNull();
		}

		@Test
		void returnsNewPetWhenIgnoreNewIsFalse() {
			owner.addPet(newPet);
			assertThat(owner.getPet("Luna", false)).isEqualTo(newPet);
		}

		@Test
		void returnsNullForNewPetWhenIgnoreNewIsTrue() {
			owner.addPet(newPet);
			assertThat(owner.getPet("Luna", true)).isNull();
		}

		@Test
		void returnsSavedPetWhenIgnoreNewIsTrue() {
			assertThat(owner.getPet("Buddy", true)).isEqualTo(savedPet);
		}

	}

	@Nested
	class GetPetById {

		@BeforeEach
		void addSavedPet() {
			owner.getPets().add(savedPet);
		}

		@Test
		void returnsPetById() {
			assertThat(owner.getPet(10)).isEqualTo(savedPet);
		}

		@Test
		void returnsNullWhenIdNotFound() {
			assertThat(owner.getPet(99)).isNull();
		}

		@Test
		void doesNotReturnNewPetById() {
			owner.addPet(newPet);
			assertThat(owner.getPet(99)).isNull();
		}

	}

	@Nested
	class AddVisit {

		@BeforeEach
		void addSavedPet() {
			owner.getPets().add(savedPet);
		}

		@Test
		void addsVisitToCorrectPet() {
			Visit visit = new Visit();
			visit.setDescription("Annual checkup");
			owner.addVisit(savedPet.getId(), visit);
			assertThat(savedPet.getVisits()).contains(visit);
		}

		@Test
		void throwsWhenPetIdIsNull() {
			Visit visit = new Visit();
			visit.setDescription("checkup");
			assertThatThrownBy(() -> owner.addVisit(null, visit)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Pet identifier must not be null");
		}

		@Test
		void throwsWhenVisitIsNull() {
			assertThatThrownBy(() -> owner.addVisit(savedPet.getId(), null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Visit must not be null");
		}

		@Test
		void throwsWhenPetIdDoesNotExist() {
			Visit visit = new Visit();
			visit.setDescription("checkup");
			assertThatThrownBy(() -> owner.addVisit(999, visit)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Invalid Pet identifier");
		}

	}

}
