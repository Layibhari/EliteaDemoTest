package org.springframework.samples.petclinic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VisitService {

	private final OwnerRepository ownerRepository;

	public VisitService(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	@Transactional(readOnly = true)
	public List<Visit> findAll() {
		List<Visit> visits = new ArrayList<>();
		for (Owner owner : ownerRepository.findAll()) {
			for (Pet pet : owner.getPets()) {
				visits.addAll(pet.getVisits());
			}
		}
		return visits;
	}

	@Transactional(readOnly = true)
	public Optional<Visit> findById(Integer id) {
		for (Owner owner : ownerRepository.findAll()) {
			for (Pet pet : owner.getPets()) {
				for (Visit visit : pet.getVisits()) {
					if (visit.getId() != null && visit.getId().equals(id)) {
						return Optional.of(visit);
					}
				}
			}
		}
		return Optional.empty();
	}

	@Transactional
	public Visit save(Integer ownerId, Integer petId, Visit visit) {
		Owner owner = ownerRepository.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException("Pet with id " + petId + " not found for owner " + ownerId);
		}
		pet.addVisit(visit);
		ownerRepository.save(owner);

		// Find the newly saved visit (assuming it's the latest one added or has an ID
		// now)
		return pet.getVisits()
			.stream()
			.filter(v -> v.getDescription().equals(visit.getDescription()) && v.getDate().equals(visit.getDate()))
			.findFirst()
			.orElse(visit);
	}

}
