package org.springframework.samples.petclinic.owner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")

public class PetRestController {

	private final OwnerRepository ownerRepository;

	public PetRestController(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	@GetMapping("/pets")
	public List<Pet> getPets(
		@RequestParam(required = false) String type,
		@RequestParam(required = false) String name) {

		List<Pet> result = new ArrayList<>();


		List<Owner> owners = ownerRepository.findAll();

		for (Owner owner : owners) {
			for (Pet pet : owner.getPets()) {

				boolean matchesType = (type == null || pet.getType().getName().equalsIgnoreCase(type));
				boolean matchesName = (name == null || pet.getName().equalsIgnoreCase(name));

				if (matchesType && matchesName) {
					result.add(pet);
				}
			}
		}

		return result;
	}

	// Returns only pet name and type with optional filtering by type and name
	@GetMapping("/pets/names")
	public List<PetResponse> getPetNames(
		@RequestParam(required = false) String type,
		@RequestParam(required = false) String name) {

		List<PetResponse> result = new ArrayList<>();

		List<Owner> owners = ownerRepository.findAll();

		for (Owner owner : owners) {
			for (Pet pet : owner.getPets()) {

				boolean matchesType = (type == null || pet.getType().getName().equalsIgnoreCase(type));
				boolean matchesName = (name == null || pet.getName().equalsIgnoreCase(name));

				if (matchesType && matchesName) {
					result.add(new PetResponse(
						pet.getName(),
						pet.getType().getName()
					));
				}
			}
		}

		return result;
	}
}

