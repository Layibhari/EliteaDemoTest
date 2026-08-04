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
package org.springframework.samples.petclinic.pet;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.samples.petclinic.owner.PetValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Dedicated "Pet Registration" section, separate from the in-line "Add Pet"
 * flow on an owner's detail page.
 * <p>
 * This controller does <b>not</b> introduce a new {@code Pet} entity or a new
 * database table. Pets in this application already belong to an
 * {@link Owner} (see the {@code pets} table, which has an {@code owner_id}
 * foreign key), so this controller reuses the existing {@link Owner},
 * {@link Pet} and {@link PetType} domain classes and repositories. It simply
 * gives staff a dedicated entry point: search for an owner, then register a
 * pet for them.
 */
@Controller
class PetRegistrationController {

	private static final String VIEW_SEARCH_OWNER = "pets/registerSearchOwner";

	private static final String VIEW_SEARCH_RESULTS = "pets/registerSearchResults";

	private static final String VIEW_REGISTER_PET = "pets/registerPet";

	private final OwnerRepository owners;

	private final PetTypeRepository types;

	public PetRegistrationController(OwnerRepository owners, PetTypeRepository types) {
		this.owners = owners;
		this.types = types;
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.types.findPetTypes();
	}

	@InitBinder("pet")
	public void initPetBinder(WebDataBinder dataBinder) {
		dataBinder.setValidator(new PetValidator());
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Step 1: show the "find the owner to register a pet for" search form.
	 */
	@GetMapping("/pets/register")
	public String initSearchForm() {
		return VIEW_SEARCH_OWNER;
	}

	/**
	 * Step 2: search owners by last name and either jump straight to the
	 * registration form (single match) or show a pick-list (multiple
	 * matches).
	 */
	@PostMapping("/pets/register")
	public String processSearchForm(@RequestParam(defaultValue = "") String lastName, Model model) {
		Page<Owner> results = this.owners.findByLastNameStartingWith(lastName, Pageable.unpaged());

		if (results.isEmpty()) {
			model.addAttribute("error", "No owners found with that last name");
			return VIEW_SEARCH_OWNER;
		}

		if (results.getTotalElements() == 1) {
			Owner owner = results.iterator().next();
			return "redirect:/pets/register/" + owner.getId();
		}

		model.addAttribute("owners", results.getContent());
		return VIEW_SEARCH_RESULTS;
	}

	/**
	 * Step 3: show the registration form for a specific owner.
	 */
	@GetMapping("/pets/register/{ownerId}")
	public String initRegistrationForm(@PathVariable("ownerId") int ownerId, Model model) {
		Owner owner = findOwnerOrThrow(ownerId);
		Pet pet = new Pet();
		owner.addPet(pet);
		model.addAttribute("owner", owner);
		model.addAttribute("pet", pet);
		return VIEW_REGISTER_PET;
	}

	/**
	 * Step 4: validate and save the new pet against the chosen owner.
	 */
	@PostMapping("/pets/register/{ownerId}")
	public String processRegistrationForm(@PathVariable("ownerId") int ownerId, @Valid Pet pet, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {

		Owner owner = findOwnerOrThrow(ownerId);

		if (StringUtils.hasText(pet.getName()) && pet.isNew() && owner.getPet(pet.getName(), true) != null) {
			result.rejectValue("name", "duplicate", "already exists");
		}

		LocalDate currentDate = LocalDate.now();
		if (pet.getBirthDate() != null && pet.getBirthDate().isAfter(currentDate)) {
			result.rejectValue("birthDate", "typeMismatch.birthDate");
		}

		if (result.hasErrors()) {
			model.addAttribute("owner", owner);
			return VIEW_REGISTER_PET;
		}

		try {
			owner.addPet(pet);
			this.owners.saveAndFlush(owner);
		}
		catch (DataIntegrityViolationException ex) {
			if (!isDuplicatePetNameViolation(ex)) {
				throw ex;
			}
			result.rejectValue("name", "duplicate", "already exists");
			model.addAttribute("owner", owner);
			return VIEW_REGISTER_PET;
		}

		redirectAttributes.addFlashAttribute("message", pet.getName() + " has been registered to " + owner.getFirstName()
				+ " " + owner.getLastName());
		return "redirect:/owners/{ownerId}";
	}

	private Owner findOwnerOrThrow(int ownerId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		return optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct"));
	}

	private boolean isDuplicatePetNameViolation(DataIntegrityViolationException ex) {
		String message = ex.getMessage();
		return message != null && message.toLowerCase().contains("unique_owner_pet_name");
	}

}
