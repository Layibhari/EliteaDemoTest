package org.springframework.samples.petclinic.service;

import java.util.Collection;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PetService {

	private final OwnerRepository ownerRepository;

	private final PetTypeRepository petTypeRepository;

	public PetService(OwnerRepository ownerRepository, PetTypeRepository petTypeRepository) {
		this.ownerRepository = ownerRepository;
		this.petTypeRepository = petTypeRepository;
	}

	@Transactional(readOnly = true)
	public Pet findPet(Integer ownerId, Integer petId) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet with id " + petId + " not found for owner " + ownerId);
		}
		return pet;
	}

	@Transactional
	public Pet save(Owner owner, Pet pet) {
		Integer id = pet.getId();
		if (id != null) {
			Pet existingPet = owner.getPet(id);
			if (existingPet != null) {
				existingPet.setName(pet.getName());
				existingPet.setBirthDate(pet.getBirthDate());
				existingPet.setType(pet.getType());
			}
			else {
				owner.addPet(pet);
			}
		}
		else {
			owner.addPet(pet);
		}
		ownerRepository.save(owner);
		// Return the saved pet. If it was new, getting it back from the owner gets the
		// saved instance
		return pet.isNew() ? owner.getPet(pet.getName(), false) : owner.getPet(pet.getId());
	}

	@Transactional(readOnly = true)
	public Collection<PetType> findPetTypes() {
		return petTypeRepository.findPetTypes();
	}

}
