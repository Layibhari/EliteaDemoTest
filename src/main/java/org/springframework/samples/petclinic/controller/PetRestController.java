package org.springframework.samples.petclinic.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.PetDTO;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.service.OwnerService;
import org.springframework.samples.petclinic.service.PetService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "Pet management REST API")
public class PetRestController {

	private final PetService petService;

	private final OwnerService ownerService;

	private final PetMapper petMapper;

	public PetRestController(PetService petService, OwnerService ownerService, PetMapper petMapper) {
		this.petService = petService;
		this.ownerService = ownerService;
		this.petMapper = petMapper;
	}

	@Operation(summary = "Get a pet by owner and pet id")
	@GetMapping
	public ResponseEntity<PetDTO> getPet(@RequestParam Integer ownerId, @RequestParam Integer petId) {
		try {
			Pet pet = petService.findPet(ownerId, petId);
			return ResponseEntity.ok(petMapper.toDto(pet));
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@Operation(summary = "Create a new pet for an owner")
	@PostMapping
	public ResponseEntity<PetDTO> createPet(@Valid @RequestBody PetDTO petDto) {
		if (petDto.getOwnerId() == null) {
			return ResponseEntity.badRequest().build(); // Owner ID is required
		}
		Owner owner = ownerService.findById(petDto.getOwnerId()).orElse(null);
		if (owner == null) {
			return ResponseEntity.notFound().build();
		}

		Pet petToSave = petMapper.toEntity(petDto);
		petToSave.setId(null); // Ensure it's treated as new
		Pet savedPet = petService.save(owner, petToSave);

		return ResponseEntity.status(HttpStatus.CREATED).body(petMapper.toDto(savedPet));
	}

	@Operation(summary = "Update an existing pet")
	@PutMapping("/{petId}")
	public ResponseEntity<PetDTO> updatePet(@PathVariable("petId") int petId, @Valid @RequestBody PetDTO petDto) {
		if (petDto.getOwnerId() == null) {
			return ResponseEntity.badRequest().build();
		}
		Owner owner = ownerService.findById(petDto.getOwnerId()).orElse(null);
		if (owner == null) {
			return ResponseEntity.notFound().build();
		}

		try {
			Pet existingPet = petService.findPet(owner.getId(), petId);
			existingPet.setName(petDto.getName());
			existingPet.setBirthDate(petDto.getBirthDate());
			existingPet.setType(petMapper.toEntity(petDto).getType());

			Pet savedPet = petService.save(owner, existingPet);
			return ResponseEntity.ok(petMapper.toDto(savedPet));
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

}
