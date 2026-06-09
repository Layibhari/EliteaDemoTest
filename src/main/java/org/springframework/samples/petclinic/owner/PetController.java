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

import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web Controller managing pet lifecycle (creation, updates, types) under an owner's path
 * context.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Wick Dynex
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	/**
	 * Relative Thymeleaf view path to render pet creation or modification form.
	 */
	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	/**
	 * Repository providing database operations for owners.
	 */
	private final OwnerRepository owners;

	/**
	 * Repository providing database operations for pet types.
	 */
	private final PetTypeRepository types;

	/**
	 * Constructor injecting repository dependencies.
	 * @param owners OwnerRepository dependency
	 * @param types PetTypeRepository dependency
	 */
	public PetController(OwnerRepository owners, PetTypeRepository types) {
		this.owners = owners;
		this.types = types;
	}

	/**
	 * Populates the selection list of pet types for select dropdown options in HTML
	 * forms.
	 * @return collection of available PetType entities
	 */
	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.types.findPetTypes();
	}

	/**
	 * Pre-fetches the owner corresponding to "ownerId" URL variable.
	 * @param ownerId path variable containing owner database ID
	 * @return Owner entity found
	 * @throws IllegalArgumentException if the owner is not found
	 */
	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		return owner;
	}

	/**
	 * Helper method to fetch the target pet instance by "petId". If no "petId" is present
	 * in the URL, instantiates and returns a blank new Pet object.
	 * @param ownerId owner ID
	 * @param petId optional pet ID parameter
	 * @return Pet object matching the ID, or a new Pet
	 */
	@ModelAttribute("pet")
	public Pet findPet(@PathVariable("ownerId") int ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {

		// If no pet ID is specified, return new transient pet instance
		if (petId == null) {
			return new Pet();
		}

		// Otherwise, lookup parent owner and return the associated pet
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		return owner.getPet(petId);
	}

	/**
	 * Configures custom request binder properties for Owner attributes. Disallows
	 * modification of ID parameter to prevent security tampering.
	 * @param dataBinder Spring's request parameter binder
	 */
	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Configures custom request binder properties for Pet attributes. Registers
	 * PetValidator and disallows direct parameter binding on the pet ID.
	 * @param dataBinder Spring's request parameter binder
	 */
	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Initializes the form for registering a new Pet.
	 * @param owner the resolved owner model attribute
	 * @param model UI model context container
	 * @return Thymeleaf template name
	 */
	@GetMapping("/pets/new")
	public String initCreationForm(Owner owner, ModelMap model) {
		Pet pet = new Pet();
		owner.addPet(pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes form submission for adding a new Pet.
	 * @param owner resolved parent owner
	 * @param pet validated pet attributes
	 * @param result validation errors registry
	 * @param redirectAttributes redirect attributes container
	 * @return redirect path to owner details, or form page on validation failure
	 */
	@PostMapping("/pets/new")
	public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result,
			RedirectAttributes redirectAttributes) {

		// Check if a pet with the same name already exists for the owner
		if (StringUtils.hasText(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}

		// Ensure pet birth date is not set in the future
		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		// Return to form if validation issues are detected
		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		// Associate pet with owner, save changes to DB, and flash a success alert message
		owner.addPet(pet);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "New Pet has been Added");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Initializes the form for editing an existing Pet.
	 * @return Thymeleaf template name
	 */
	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm() {
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes form submission for modifying details of an existing Pet.
	 * @param owner parent owner
	 * @param pet validated pet object
	 * @param result validation errors container
	 * @param redirectAttributes redirect attributes container
	 * @return redirect to owner details page, or form view if validation fails
	 */
	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(Owner owner, @Valid Pet pet, BindingResult result,
			RedirectAttributes redirectAttributes) {

		String petName = pet.getName();

		// Check if the modified name collides with another existing pet's name
		if (StringUtils.hasText(petName)) {
			Pet existingPet = owner.getPet(petName, false);
			// Reject if name exists on another pet instance that has a different ID
			if (existingPet != null && !Objects.equals(existingPet.getId(), pet.getId())) {
				result.rejectValue("name", "duplicate", "already exists");
			}
		}

		// Ensure pet birth date is not set in the future
		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		// Return to form on validation failure
		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		// Update properties and save updates to database
		updatePetDetails(owner, pet);
		redirectAttributes.addFlashAttribute("message", "Pet details has been edited");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Helper method that updates the properties of an existing pet or registers a new pet
	 * under the owner.
	 * @param owner The owner of the pet
	 * @param pet The pet object holding the updated properties
	 */
	private void updatePetDetails(Owner owner, Pet pet) {
		Integer id = pet.getId();
		Assert.state(id != null, "'pet.getId()' must not be null");
		Pet existingPet = owner.getPet(id);
		if (existingPet != null) {
			// Update properties on the already-managed hibernate entity
			existingPet.setName(pet.getName());
			existingPet.setBirthDate(pet.getBirthDate());
			existingPet.setType(pet.getType());
		}
		else {
			// Add as new pet relationship
			owner.addPet(pet);
		}
		// Save the cascading update state
		this.owners.save(owner);
	}

}
