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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * REST API Controller for Owner management.
 * Provides CRUD operations for pet owners.
 *
 * @author Spring PetClinic Team
 */
@RestController
@RequestMapping("/api/owners")
class OwnerApiController {

	private final OwnerRepository ownerRepository;

	public OwnerApiController(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	/**
	 * Get all owners with pagination
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> getAllOwners(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "lastName") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir) {

		Sort sort = sortDir.equalsIgnoreCase("desc")
				? Sort.by(sortBy).descending()
				: Sort.by(sortBy).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<Owner> ownerPage = ownerRepository.findAll(pageable);

		Map<String, Object> response = new HashMap<>();
		response.put("owners", ownerPage.getContent());
		response.put("currentPage", ownerPage.getNumber());
		response.put("totalItems", ownerPage.getTotalElements());
		response.put("totalPages", ownerPage.getTotalPages());
		response.put("pageSize", ownerPage.getSize());

		return ResponseEntity.ok(response);
	}

	/**
	 * Get owner by ID
	 */
	@GetMapping("/{ownerId}")
	public ResponseEntity<Owner> getOwnerById(@PathVariable("ownerId") int ownerId) {
		return ownerRepository.findById(ownerId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Search owners by last name
	 */
	@GetMapping("/search")
	public ResponseEntity<List<Owner>> searchOwners(@RequestParam String lastName) {
		List<Owner> owners = ownerRepository.findByLastNameStartingWith(lastName);
		return ResponseEntity.ok(owners);
	}

	/**
	 * Create new owner
	 */
	@PostMapping
	public ResponseEntity<Owner> createOwner(@Valid @RequestBody Owner owner) {
		Owner savedOwner = ownerRepository.save(owner);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedOwner);
	}

	/**
	 * Update existing owner
	 */
	@PutMapping("/{ownerId}")
	public ResponseEntity<Owner> updateOwner(
			@PathVariable("ownerId") int ownerId,
			@Valid @RequestBody Owner owner) {

		return ownerRepository.findById(ownerId)
				.map(existingOwner -> {
					existingOwner.setFirstName(owner.getFirstName());
					existingOwner.setLastName(owner.getLastName());
					existingOwner.setAddress(owner.getAddress());
					existingOwner.setCity(owner.getCity());
					existingOwner.setTelephone(owner.getTelephone());
					Owner updated = ownerRepository.save(existingOwner);
					return ResponseEntity.ok(updated);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Delete owner by ID
	 */
	@DeleteMapping("/{ownerId}")
	public ResponseEntity<Void> deleteOwner(@PathVariable("ownerId") int ownerId) {
		if (ownerRepository.existsById(ownerId)) {
			ownerRepository.deleteById(ownerId);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

	/**
	 * Get owner with pets (eager fetch)
	 */
	@GetMapping("/{ownerId}/with-pets")
	public ResponseEntity<Owner> getOwnerWithPets(@PathVariable("ownerId") int ownerId) {
		return ownerRepository.findById(ownerId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

}
