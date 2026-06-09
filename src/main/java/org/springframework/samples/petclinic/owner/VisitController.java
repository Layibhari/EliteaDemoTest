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
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web Controller managing pet visits lifecycle creation.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 * @author Wick Dynex
 */
@Controller
class VisitController {

	/**
	 * Repository providing database operations for owners.
	 */
	private final OwnerRepository owners;

	/**
	 * Constructor injecting the owner repository.
	 * @param owners OwnerRepository dependency
	 */
	public VisitController(OwnerRepository owners) {
		this.owners = owners;
	}

	/**
	 * Configures custom request binder properties. Disallows modification of ID parameter
	 * to prevent security tampering.
	 * @param dataBinder Spring's request parameter binder
	 */
	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Preload method called before every RequestMapping method. Ensures fresh Owner and
	 * Pet data are loaded from DB, populates the UI model attributes, and initializes a
	 * new transient Visit associated with the target pet.
	 * @param ownerId owner path variable ID
	 * @param petId pet path variable ID
	 * @param model UI model context container mapping attributes
	 * @return transient Visit initialized and associated with pet
	 * @throws IllegalArgumentException if the owner or pet is not found
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		// Fetch owner from repository
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

		// Resolve associated pet
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}

		// Map UI form parameters
		model.put("pet", pet);
		model.put("owner", owner);

		// Initialize visit relation
		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}

	/**
	 * Model attribute defining the minimum acceptable visit date (configured to
	 * tomorrow). Used for client/server validation checks.
	 * @return tomorrow's LocalDate value
	 */
	@ModelAttribute("minVisitDate")
	public LocalDate minVisitDate() {
		return LocalDate.now().plusDays(1);
	}

	/**
	 * Initializes the form view for recording a new visit. Spring MVC invokes
	 * loadPetWithVisit(...) first to prepare the form object mapping.
	 * @return Thymeleaf view template name
	 */
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm() {
		return "pets/createOrUpdateVisitForm";
	}

	/**
	 * Processes the form submission for recording a new visit. Spring MVC invokes
	 * loadPetWithVisit(...) first to resolve the owner, pet and visit.
	 * @param owner resolved parent owner attribute
	 * @param petId URL pet ID parameter
	 * @param visit validated visit model attribute
	 * @param result validation errors registry
	 * @param redirectAttributes redirect attributes container
	 * @return details view redirect, or form view on failure
	 */
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Visit visit,
			BindingResult result, RedirectAttributes redirectAttributes) {

		// Custom validation rule: ensure visit date is strictly in the future (tomorrow
		// or later)
		if (visit.getDate() != null && !visit.getDate().isAfter(LocalDate.now())) {
			result.rejectValue("date", "typeMismatch.visitDate");
		}

		// Re-render form template on validation errors
		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}

		// Associate visit details under the target pet ID and persist changes
		owner.addVisit(petId, visit);
		this.owners.save(owner);

		// Add flash success alert and redirect to owner details page
		redirectAttributes.addFlashAttribute("message", "Your visit has been booked");
		return "redirect:/owners/{ownerId}";
	}

}
