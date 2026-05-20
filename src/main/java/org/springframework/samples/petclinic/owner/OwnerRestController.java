package org.springframework.samples.petclinic.owner;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Owner;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

/**
 * REST controller for managing Owner entities via API endpoints.
 * <p>
 * <b>Note:</b> This controller is intended for API REST testing purposes only and should
 * not be used for production HTML views.
 */
@RestController
@RequestMapping("api/owners")
public class OwnerRestController {

	private final OwnerRepository owners;

	/**
	 * Constructor for dependency injection of OwnerRepository.
	 * @param owners the repository for Owner entities
	 */
	public OwnerRestController(OwnerRepository owners) {
		this.owners = owners;
	}

	/**
	 * Retrieve an Owner by its ID.
	 * @param id the Owner's ID
	 * @return the Owner if found, or 404 Not Found
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Owner> findOwner(@PathVariable int id) {
		return owners.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/**
	 * Create a new Owner.
	 * @param owner the Owner to create
	 * @return the created Owner with HTTP 201 status
	 */
	@PostMapping
	public ResponseEntity<Owner> createOwner(@RequestBody Owner owner) {
		Owner savedOwner = owners.save(owner);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedOwner);
	}

	/**
	 * Update an existing Owner by ID.
	 * @param id the Owner's ID
	 * @param ownerDetails the updated Owner data
	 * @return the updated Owner if found, or 404 Not Found
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Owner> updateOwner(@PathVariable int id, @RequestBody Owner ownerDetails) {
		return owners.findById(id).map(owner -> {
			owner.setFirstName(ownerDetails.getFirstName());
			owner.setLastName(ownerDetails.getLastName());
			owner.setAddress(ownerDetails.getAddress());
			owner.setCity(ownerDetails.getCity());
			owner.setTelephone(ownerDetails.getTelephone());
			Owner updatedOwner = owners.save(owner);
			return ResponseEntity.ok(updatedOwner);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	/**
	 * Delete an Owner by ID.
	 * <p>
	 * <b>Note:</b> This operation should only be used for Owners that have no pets
	 * associated.
	 * @param id the Owner's ID
	 * @return HTTP 204 No Content if deleted, or 404 Not Found
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOwner(@PathVariable int id) {
		return owners.findById(id).map(owner -> {
			owners.deleteById(id);
			return ResponseEntity.noContent().<Void>build();
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

}