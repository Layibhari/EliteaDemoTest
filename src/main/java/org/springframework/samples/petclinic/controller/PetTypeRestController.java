package org.springframework.samples.petclinic.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.PetTypeDTO;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.service.PetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pettypes")
@Tag(name = "Pet Types", description = "Pet Types catalog API")
public class PetTypeRestController {

	private final PetService petService;

	private final PetMapper petMapper;

	public PetTypeRestController(PetService petService, PetMapper petMapper) {
		this.petService = petService;
		this.petMapper = petMapper;
	}

	@Operation(summary = "Get all pet types")
	@GetMapping
	public ResponseEntity<List<PetTypeDTO>> getPetTypes() {
		Collection<org.springframework.samples.petclinic.owner.PetType> petTypes = petService.findPetTypes();
		List<PetTypeDTO> dtoList = petTypes.stream().map(petMapper::toDto).collect(Collectors.toList());
		return ResponseEntity.ok(dtoList);
	}

}
