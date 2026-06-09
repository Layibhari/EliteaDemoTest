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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.core.style.ToStringCreator;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.util.Assert;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

/**
 * Simple JavaBean domain object representing an owner.
 *
 * Configured as a JPA Entity mapped to the "owners" table. Inherits basic personal
 * details from Person class.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 * @author Oliver Drotbohm
 * @author Wick Dynex
 */
@Entity
@Table(name = "owners")
public class Owner extends Person {

	/**
	 * Street address of the owner. Cannot be empty.
	 */
	@Column
	@NotBlank
	private String address;

	/**
	 * City of residence of the owner. Cannot be empty.
	 */
	@Column
	@NotBlank
	private String city;

	/**
	 * Telephone number of the owner. Must be exactly 10 digits as specified by the
	 * Pattern validator.
	 */
	@Column
	@NotBlank
	@Pattern(regexp = "\\d{10}", message = "{telephone.invalid}")
	private String telephone;

	/**
	 * Bidirectional one-to-many relationship with Pets. Cascade ALL ensures that
	 * saving/deleting owners cascades to their pets. Fetched EAGERly to ensure pet lists
	 * are loaded alongside owner details. Pets are ordered lexicographically by name.
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "owner_id")
	@OrderBy("name")
	private final List<Pet> pets = new ArrayList<>();

	/**
	 * Gets the owner's street address.
	 * @return address string
	 */
	public String getAddress() {
		return this.address;
	}

	/**
	 * Sets the owner's street address.
	 * @param address street address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Gets the owner's city of residence.
	 * @return city string
	 */
	public String getCity() {
		return this.city;
	}

	/**
	 * Sets the owner's city of residence.
	 * @param city city string
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Gets the owner's telephone number.
	 * @return telephone string
	 */
	public String getTelephone() {
		return this.telephone;
	}

	/**
	 * Sets the owner's telephone number.
	 * @param telephone 10-digit telephone string
	 */
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	/**
	 * Gets the list of pets owned by this owner.
	 * @return list of Pet objects
	 */
	public List<Pet> getPets() {
		return this.pets;
	}

	/**
	 * Adds a pet to the owner's pet list. Only adds the pet if the pet is new (i.e. not
	 * yet saved/associated).
	 * @param pet the pet to add
	 */
	public void addPet(Pet pet) {
		if (pet.isNew()) {
			getPets().add(pet);
		}
	}

	/**
	 * Return the Pet with the given name, or null if none found for this Owner.
	 * @param name to test
	 * @return the Pet with the given name, or null if no such Pet exists for this Owner
	 */
	public Pet getPet(String name) {
		return getPet(name, false);
	}

	/**
	 * Return the Pet with the given id, or null if none found for this Owner.
	 * @param id to test
	 * @return the Pet with the given id, or null if no such Pet exists for this Owner
	 */
	public Pet getPet(Integer id) {
		// Iterate through all associated pets
		for (Pet pet : getPets()) {
			// Compare IDs only if the pet is not new (meaning it has a valid database
			// identifier)
			if (!pet.isNew()) {
				Integer compId = pet.getId();
				if (Objects.equals(compId, id)) {
					return pet;
				}
			}
		}
		return null;
	}

	/**
	 * Return the Pet with the given name, or null if none found for this Owner.
	 * @param name to test
	 * @param ignoreNew whether to ignore new pets (pets that are not saved yet)
	 * @return the Pet with the given name, or null if no such Pet exists for this Owner
	 */
	public Pet getPet(String name, boolean ignoreNew) {
		// Iterate through all associated pets to match by name (case-insensitive)
		for (Pet pet : getPets()) {
			String compName = pet.getName();
			if (compName != null && compName.equalsIgnoreCase(name)) {
				// Return pet if ignoreNew is false or if the pet is already persisted in
				// DB
				if (!ignoreNew || !pet.isNew()) {
					return pet;
				}
			}
		}
		return null;
	}

	/**
	 * Generates a string representation of the Owner object using ToStringCreator.
	 * @return formatted string listing key owner properties
	 */
	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", this.getId())
			.append("new", this.isNew())
			.append("lastName", this.getLastName())
			.append("firstName", this.getFirstName())
			.append("address", this.address)
			.append("city", this.city)
			.append("telephone", this.telephone)
			.toString();
	}

	/**
	 * Adds the given {@link Visit} to the {@link Pet} with the given identifier.
	 * @param petId the identifier of the {@link Pet}, must not be {@literal null}.
	 * @param visit the visit to add, must not be {@literal null}.
	 */
	public void addVisit(Integer petId, Visit visit) {

		// Verify parameters are not null before proceeding
		Assert.notNull(petId, "Pet identifier must not be null!");
		Assert.notNull(visit, "Visit must not be null!");

		// Find the target pet by identifier
		Pet pet = getPet(petId);

		// Ensure the pet belongs to this owner
		Assert.notNull(pet, "Invalid Pet identifier!");

		// Add the visit to the pet entity
		pet.addVisit(visit);
	}

}
