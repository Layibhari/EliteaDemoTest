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
 * Handles pet-related web requests within an owner record.
 *
 * <p>
 * This class coordinates the pet creation and update flows by loading the related owner,
 * exposing available pet types, validating submitted pet data, and saving changes through
 * the owner repository.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Wick Dynex
 */
@Controller
@RequestMapping("/owners/{ownerId}")
class PetController {

	private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

	private final OwnerRepository owners;

	private final PetTypeRepository types;

	/**
	 * Creates a pet controller using repositories for owner and pet-type data.
	 * @param owners repository used to load and save owners
	 * @param types repository used to load available pet types
	 */
	public PetController(OwnerRepository owners, PetTypeRepository types) {
		this.owners = owners;
		this.types = types;
	}

	/**
	 * Adds all available pet types to the model for pet forms.
	 * @return collection of pet types available for selection
	 */
	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.types.findPetTypes();
	}

	/**
	 * Loads the owner associated with the current pet request.
	 * @param ownerId owner identifier from the request path
	 * @return the matching owner
	 * @throws IllegalArgumentException if no owner exists for the supplied identifier
	 */
	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable("ownerId") int ownerId) {
		return getRequiredOwner(ownerId);
	}

	/**
	 * Provides the pet model attribute used by pet creation and update forms.
	 *
	 * <p>
	 * A new pet is created for creation requests. For update requests, the existing pet
	 * is loaded from the owner's pet collection.
	 * @param ownerId owner identifier from the request path
	 * @param petId optional pet identifier from the request path
	 * @return a new pet or the existing pet being updated
	 */
	@ModelAttribute("pet")
	public Pet findPet(@PathVariable("ownerId") int ownerId,
			@PathVariable(name = "petId", required = false) Integer petId) {
		if (petId == null) {
			return new Pet();
		}
		return getRequiredOwner(ownerId).getPet(petId);
	}

	/**
	 * Prevents owner identifiers from being overwritten through pet form requests.
	 * @param dataBinder binder used to configure restricted owner fields
	 */
	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Configures pet form validation and prevents direct binding of pet identifiers.
	 * @param dataBinder binder used to configure pet validation and restricted fields
	 */
	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Shows the form for adding a new pet to an owner.
	 * @param owner owner that will receive the new pet
	 * @param model model map used by the view layer
	 * @return the pet creation form view
	 */
	@GetMapping("/pets/new")
	public String initCreationForm(Owner owner, ModelMap model) {
		Pet pet = new Pet();
		owner.addPet(pet);
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes a submitted form for creating a new pet.
	 *
	 * <p>
	 * The pet name is checked for duplicates within the same owner record and the birth
	 * date is checked before the pet is saved.
	 * @param owner owner that will receive the new pet
	 * @param pet pet data submitted by the user
	 * @param result validation result for the submitted pet
	 * @param redirectAttributes flash attributes used after redirects
	 * @return the form view when validation fails, or a redirect to the owner details
	 * page
	 */
	@PostMapping("/pets/new")
	public String processCreationForm(Owner owner, @Valid Pet pet, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (hasDuplicateNewPetName(owner, pet)) {
			result.rejectValue("name", "duplicate", "already exists");
		}
		if (hasBirthDateInFuture(pet)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		owner.addPet(pet);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "New Pet has been Added");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Shows the form for updating an existing pet.
	 * @return the pet update form view
	 */
	@GetMapping("/pets/{petId}/edit")
	public String initUpdateForm() {
		return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes a submitted form for updating an existing pet.
	 *
	 * <p>
	 * The submitted pet is validated before the owner's pet collection is updated and
	 * saved.
	 * @param owner owner that contains the pet being updated
	 * @param pet pet data submitted by the user
	 * @param result validation result for the submitted pet
	 * @param redirectAttributes flash attributes used after redirects
	 * @return the form view when validation fails, or a redirect to the owner details
	 * page
	 */
	@PostMapping("/pets/{petId}/edit")
	public String processUpdateForm(Owner owner, @Valid Pet pet, BindingResult result,
			RedirectAttributes redirectAttributes) {
		if (hasDuplicateUpdatedPetName(owner, pet)) {
			result.rejectValue("name", "duplicate", "already exists");
		}
		if (hasBirthDateInFuture(pet)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		if (result.hasErrors()) {
			return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
		}

		updatePetDetails(owner, pet);
		redirectAttributes.addFlashAttribute("message", "Pet details has been edited");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Updates an existing pet in the owner record, or adds it if it cannot be found.
	 * @param owner owner that contains the pet record
	 * @param pet pet data containing the updated details
	 */
	private void updatePetDetails(Owner owner, Pet pet) {
		Integer id = pet.getId();
		Assert.state(id != null, "'pet.getId()' must not be null");
		Pet existingPet = owner.getPet(id);
		if (existingPet != null) {
			// Update existing pet's properties
			existingPet.setName(pet.getName());
			existingPet.setBirthDate(pet.getBirthDate());
			existingPet.setType(pet.getType());
		}
		else {
			owner.addPet(pet);
		}
		this.owners.save(owner);
	}

	private Owner getRequiredOwner(int ownerId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		return optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
	}

	private boolean hasDuplicateNewPetName(Owner owner, Pet pet) {
		return StringUtils.hasText(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null;
	}

	private boolean hasDuplicateUpdatedPetName(Owner owner, Pet pet) {
		if (!StringUtils.hasText(pet.getName())) {
			return false;
		}
		Pet existingPet = owner.getPet(pet.getName(), false);
		return existingPet != null && !Objects.equals(existingPet.getId(), pet.getId());
	}

	private boolean hasBirthDateInFuture(Pet pet) {
		return pet.getBirthDate() != null && pet.getBirthDate().isAfter(LocalDate.now());
	}

}
