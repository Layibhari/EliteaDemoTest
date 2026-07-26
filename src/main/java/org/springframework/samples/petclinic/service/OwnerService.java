package org.springframework.samples.petclinic.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerService {

	private final OwnerRepository ownerRepository;

	public OwnerService(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	@Transactional(readOnly = true)
	public Page<Owner> findByLastNameStartingWith(String lastName, Pageable pageable) {
		return ownerRepository.findByLastNameStartingWith(lastName, pageable);
	}

	@Transactional(readOnly = true)
	public Optional<Owner> findById(Integer id) {
		return ownerRepository.findById(id);
	}

	@Transactional
	public Owner save(Owner owner) {
		return ownerRepository.save(owner);
	}

}
