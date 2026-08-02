package org.springframework.samples.petclinic.vet;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;

/**
 * Form data used when creating a veterinarian.
 */
public class VetForm {

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	private List<Integer> specialtyIds = new ArrayList<>();

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<Integer> getSpecialtyIds() {
		return this.specialtyIds;
	}

	public void setSpecialtyIds(List<Integer> specialtyIds) {
		this.specialtyIds = specialtyIds == null ? new ArrayList<>() : specialtyIds;
	}

}