package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for retrieving veterinarian specialties.
 */
public interface SpecialtyRepository extends JpaRepository<Specialty, Integer> {

	List<Specialty> findAllByOrderByNameAsc();

}