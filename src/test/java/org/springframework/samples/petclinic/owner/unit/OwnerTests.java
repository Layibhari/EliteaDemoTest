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

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit coverage for reviewed Owner domain scenarios.
 */
class OwnerTests {

	@Test
	void unit001AddPetAddsOnlyUnsavedPets() {
		Owner owner = new Owner();
		Pet unsavedPet = pet("Max", null);
		Pet savedPet = pet("Buddy", 7);

		owner.addPet(unsavedPet);
		owner.addPet(savedPet);

		assertThat(owner.getPets()).containsExactly(unsavedPet);
	}

	@Test
	void unit002GetPetByNameIsCaseInsensitiveAndReturnsNullWhenAbsent() {
		Owner owner = ownerWithPets(pet("Max", 7));

		assertThat(owner.getPet("max")).isSameAs(owner.getPets().get(0));
		assertThat(owner.getPet("MAX")).isSameAs(owner.getPets().get(0));
		assertThat(owner.getPet("Unknown")).isNull();
	}

	@Test
	void unit003GetPetByNameCanIgnoreNewUnsavedPets() {
		Pet unsavedMax = pet("Max", null);
		Owner owner = ownerWithPets(unsavedMax, pet("Buddy", 7));

		assertThat(owner.getPet("Max", true)).isNull();
		assertThat(owner.getPet("Max", false)).isSameAs(unsavedMax);
	}

	@Test
	void unit004GetPetByIdIgnoresUnsavedPetsAndMatchesSavedIds() {
		Pet savedPet = pet("Buddy", 7);
		Owner owner = ownerWithPets(pet("Max", null), savedPet);

		assertThat(owner.getPet(7)).isSameAs(savedPet);
		assertThat(owner.getPet((Integer) null)).isNull();
	}

	@Test
	void unit005AddVisitRejectsNullInputs() {
		Owner owner = ownerWithPets(pet("Buddy", 7));

		assertThatThrownBy(() -> owner.addVisit(null, visit("checkup"))).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Pet identifier must not be null!");
		assertThatThrownBy(() -> owner.addVisit(7, null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Visit must not be null!");
	}

	@Test
	void unit006AddVisitRejectsUnknownPetIdWithoutMutatingPets() {
		Pet pet = pet("Buddy", 7);
		Owner owner = ownerWithPets(pet);

		assertThatThrownBy(() -> owner.addVisit(999, visit("checkup"))).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Invalid Pet identifier!");
		assertThat(pet.getVisits()).isEmpty();
	}

	@Test
	void unit007AddVisitAddsVisitToRequestedPetOnly() {
		Pet firstPet = pet("Buddy", 7);
		Pet secondPet = pet("Max", 8);
		Owner owner = ownerWithPets(firstPet, secondPet);
		Visit visit = visit("checkup");

		owner.addVisit(8, visit);

		assertThat(firstPet.getVisits()).isEmpty();
		assertThat(secondPet.getVisits()).containsExactly(visit);
	}

	private Owner ownerWithPets(Pet... pets) {
		Owner owner = new Owner();
		owner.getPets().addAll(List.of(pets));
		return owner;
	}

	private Pet pet(String name, Integer id) {
		Pet pet = new Pet();
		pet.setName(name);
		pet.setId(id);
		return pet;
	}

	private Visit visit(String description) {
		Visit visit = new Visit();
		visit.setDescription(description);
		return visit;
	}

}
