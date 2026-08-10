package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerTests {

	@Test
	void addPetShouldAddPersistedPet() {
		Owner owner = new Owner();

		Pet pet = new Pet();
		pet.setId(5);

		owner.addPet(pet);

		assertThat(owner.getPets()).containsExactly(pet);
	}

	@Test
	void addPetShouldNotAddDuplicatePet() {
		Owner owner = new Owner();

		Pet pet = new Pet();
		pet.setId(5);

		owner.addPet(pet);
		owner.addPet(pet);

		assertThat(owner.getPets()).containsExactly(pet);
	}

}
