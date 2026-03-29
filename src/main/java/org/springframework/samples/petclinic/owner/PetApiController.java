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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST API Controller for Pet management.
 * Provides CRUD operations for pets.
 *
 * @author Spring PetClinic Team
 */
@RestController
@RequestMapping("/api/pets")
class PetApiController {

	private final PetRepository petRepository;
	private final OwnerRepository ownerRepository;

	public PetApiController(PetRepository petRepository, OwnerRepository ownerRepository) {
		this.petRepository = petRepository;
		this.ownerRepository = ownerRepository;
	}

	/**
	 * Get all pets with pagination
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> getAllPets(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Pet> petPage = petRepository.findAll(pageable);

		Map<String, Object> response = new HashMap<>();
		response.put("pets", petPage.getContent());
		response.put("currentPage", petPage.getNumber());
		response.put("totalItems", petPage.getTotalElements());
		response.put("totalPages", petPage.getTotalPages());

		return ResponseEntity.ok(response);
	}

	/**
	 * Get pet by ID
	 */
	@GetMapping("/{petId}")
	public ResponseEntity<Pet> getPetById(@PathVariable("petId") int petId) {
		return petRepository.findById(petId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Get pets by owner ID
	 */
	@GetMapping("/owner/{ownerId}")
	public ResponseEntity<List<Pet>> getPetsByOwner(@PathVariable("ownerId") int ownerId) {
		return ownerRepository.findById(ownerId)
				.map(owner -> ResponseEntity.ok(owner.getPets()))
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Create new pet for an owner
	 */
	@PostMapping("/owner/{ownerId}")
	public ResponseEntity<?> createPet(
			@PathVariable("ownerId") int ownerId,
			@Valid @RequestBody Pet pet) {

		return ownerRepository.findById(ownerId)
				.map(owner -> {
					pet.setOwner(owner);
					if (pet.getBirthDate() == null) {
						pet.setBirthDate(LocalDate.now());
					}
					Pet savedPet = petRepository.save(pet);

					Map<String, Object> response = new HashMap<>();
					response.put("pet", savedPet);
					response.put("ownerId", ownerId);
					response.put("ownerName", owner.getFirstName() + " " + owner.getLastName());

					return ResponseEntity.status(HttpStatus.CREATED).body(response);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Update existing pet
	 */
	@PutMapping("/{petId}")
	public ResponseEntity<?> updatePet(
			@PathVariable("petId") int petId,
			@Valid @RequestBody Pet pet) {

		return petRepository.findById(petId)
				.map(existingPet -> {
					existingPet.setName(pet.getName());
					existingPet.setBirthDate(pet.getBirthDate());
					existingPet.setType(pet.getType());
					Pet updated = petRepository.save(existingPet);

					Map<String, Object> response = new HashMap<>();
					response.put("pet", updated);
					response.put("ownerId", existingPet.getOwner().getId());

					return ResponseEntity.ok(response);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Delete pet by ID
	 */
	@DeleteMapping("/{petId}")
	public ResponseEntity<Void> deletePet(@PathVariable("petId") int petId) {
		if (petRepository.existsById(petId)) {
			petRepository.deleteById(petId);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

}
