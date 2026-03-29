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
 * REST API Controller for Visit management.
 * Provides CRUD operations for pet visits.
 *
 * @author Spring PetClinic Team
 */
@RestController
@RequestMapping("/api")
class VisitApiController {

	private final VisitRepository visitRepository;
	private final PetRepository petRepository;

	public VisitApiController(VisitRepository visitRepository, PetRepository petRepository) {
		this.visitRepository = visitRepository;
		this.petRepository = petRepository;
	}

	/**
	 * Get all visits
	 */
	@GetMapping("/visits")
	public ResponseEntity<List<Visit>> getAllVisits() {
		return ResponseEntity.ok(visitRepository.findAll());
	}

	/**
	 * Get visits for a specific pet
	 */
	@GetMapping("/pets/{petId}/visits")
	public ResponseEntity<List<Visit>> getVisitsForPet(@PathVariable("petId") int petId) {
		return petRepository.findById(petId)
				.map(pet -> ResponseEntity.ok(pet.getVisits()))
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Get visit by ID
	 */
	@GetMapping("/visits/{visitId}")
	public ResponseEntity<Visit> getVisitById(@PathVariable("visitId") int visitId) {
		return visitRepository.findById(visitId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Create new visit for a pet
	 */
	@PostMapping("/pets/{petId}/visits")
	public ResponseEntity<?> createVisit(
			@PathVariable("petId") int petId,
			@Valid @RequestBody Visit visit) {

		return petRepository.findById(petId)
				.map(pet -> {
					visit.setPet(pet);
					if (visit.getDate() == null) {
						visit.setDate(LocalDate.now());
					}
					Visit savedVisit = visitRepository.save(visit);

					Map<String, Object> response = new HashMap<>();
					response.put("visit", savedVisit);
					response.put("petId", petId);
					response.put("petName", pet.getName());

					return ResponseEntity.status(HttpStatus.CREATED).body(response);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Update existing visit
	 */
	@PutMapping("/visits/{visitId}")
	public ResponseEntity<Visit> updateVisit(
			@PathVariable("visitId") int visitId,
			@Valid @RequestBody Visit visit) {

		return visitRepository.findById(visitId)
				.map(existingVisit -> {
					existingVisit.setDate(visit.getDate());
					existingVisit.setDescription(visit.getDescription());
					Visit updated = visitRepository.save(existingVisit);
					return ResponseEntity.ok(updated);
				})
				.orElse(ResponseEntity.notFound().build());
	}

	/**
	 * Delete visit by ID
	 */
	@DeleteMapping("/visits/{visitId}")
	public ResponseEntity<Void> deleteVisit(@PathVariable("visitId") int visitId) {
		if (visitRepository.existsById(visitId)) {
			visitRepository.deleteById(visitId);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}

}
