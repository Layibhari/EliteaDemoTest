package org.springframework.samples.petclinic.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.OwnerDTO;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.service.OwnerService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/owners")
@Tag(name = "Owners", description = "Owner management REST API")
public class OwnerRestController {

	private final OwnerService ownerService;

	private final OwnerMapper ownerMapper;

	public OwnerRestController(OwnerService ownerService, OwnerMapper ownerMapper) {
		this.ownerService = ownerService;
		this.ownerMapper = ownerMapper;
	}

	@Operation(summary = "Get owners by last name (or all if not provided)")
	@GetMapping
	public ResponseEntity<List<OwnerDTO>> getOwners(@RequestParam(required = false, defaultValue = "") String lastName,
			@RequestParam(required = false, defaultValue = "1") int page) {
		int pageSize = 100; // Large page size to emulate list all for API
		Page<Owner> ownersPage = ownerService.findByLastNameStartingWith(lastName, PageRequest.of(page - 1, pageSize));
		if (ownersPage.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		List<OwnerDTO> dtoList = ownersPage.getContent().stream().map(ownerMapper::toDto).collect(Collectors.toList());
		return ResponseEntity.ok(dtoList);
	}

	@Operation(summary = "Get an owner by ID")
	@GetMapping("/{id}")
	public ResponseEntity<OwnerDTO> getOwner(@PathVariable("id") int id) {
		Optional<Owner> owner = ownerService.findById(id);
		return owner.map(value -> ResponseEntity.ok(ownerMapper.toDto(value)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Create a new owner")
	@PostMapping
	public ResponseEntity<OwnerDTO> createOwner(@Valid @RequestBody OwnerDTO ownerDto) {
		Owner ownerToSave = ownerMapper.toEntity(ownerDto);
		// clear id if it was mistakenly passed
		ownerToSave.setId(null);
		Owner savedOwner = ownerService.save(ownerToSave);
		return ResponseEntity.status(HttpStatus.CREATED).body(ownerMapper.toDto(savedOwner));
	}

	@Operation(summary = "Update an existing owner")
	@PutMapping("/{id}")
	public ResponseEntity<OwnerDTO> updateOwner(@PathVariable("id") int id, @Valid @RequestBody OwnerDTO ownerDto) {
		Optional<Owner> optionalOwner = ownerService.findById(id);
		if (!optionalOwner.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		Owner ownerToUpdate = optionalOwner.get();
		ownerToUpdate.setFirstName(ownerDto.getFirstName());
		ownerToUpdate.setLastName(ownerDto.getLastName());
		ownerToUpdate.setAddress(ownerDto.getAddress());
		ownerToUpdate.setCity(ownerDto.getCity());
		ownerToUpdate.setTelephone(ownerDto.getTelephone());

		Owner savedOwner = ownerService.save(ownerToUpdate);
		return ResponseEntity.ok(ownerMapper.toDto(savedOwner));
	}

}
