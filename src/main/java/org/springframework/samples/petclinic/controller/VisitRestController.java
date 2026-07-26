package org.springframework.samples.petclinic.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.dto.VisitDTO;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.service.VisitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/visits")
@Tag(name = "Visits", description = "Visit management REST API")
public class VisitRestController {

	private final VisitService visitService;

	private final VisitMapper visitMapper;

	public VisitRestController(VisitService visitService, VisitMapper visitMapper) {
		this.visitService = visitService;
		this.visitMapper = visitMapper;
	}

	@Operation(summary = "Get all visits")
	@GetMapping
	public ResponseEntity<List<VisitDTO>> getAllVisits() {
		List<Visit> visits = visitService.findAll();
		List<VisitDTO> dtoList = visits.stream().map(visitMapper::toDto).collect(Collectors.toList());
		return ResponseEntity.ok(dtoList);
	}

	@Operation(summary = "Get a visit by ID")
	@GetMapping("/{id}")
	public ResponseEntity<VisitDTO> getVisit(@PathVariable("id") int id) {
		Optional<Visit> visit = visitService.findById(id);
		return visit.map(value -> ResponseEntity.ok(visitMapper.toDto(value)))
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Create a new visit for a pet")
	@PostMapping
	public ResponseEntity<VisitDTO> createVisit(@RequestParam Integer ownerId, @RequestParam Integer petId,
			@Valid @RequestBody VisitDTO visitDto) {
		Visit visit = visitMapper.toEntity(visitDto);
		visit.setId(null); // Ensure it's a new visit

		try {
			Visit savedVisit = visitService.save(ownerId, petId, visit);
			return ResponseEntity.status(HttpStatus.CREATED).body(visitMapper.toDto(savedVisit));
		}
		catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().build();
		}
	}

}
