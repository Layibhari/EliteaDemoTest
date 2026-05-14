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
 * Spring MVC controller responsible for visit-related requests for a pet.
 *
 * <p>
 * This controller supports the flow for booking a new visit. It loads the related owner
 * and pet before displaying or processing the visit form, then saves the visit through
 * {@link OwnerRepository}.
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

	private final OwnerRepository owners;

	/**
	 * Creates a visit controller using the repository that manages owner records.
	 * @param owners repository used to load owners and save visit changes
	 */
	public VisitController(OwnerRepository owners) {
		this.owners = owners;
	}

	/**
	 * Prevents request data from directly binding entity identifiers.
	 * @param dataBinder binder used to configure disallowed fields
	 */
	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Loads the owner and pet for the current visit request and prepares a visit model.
	 *
	 * <p>
	 * This method runs before the visit form handlers. It ensures that the model contains
	 * fresh owner and pet data and creates a visit instance that can be bound to the
	 * submitted form.
	 * @param ownerId identifier of the owner from the request path
	 * @param petId identifier of the pet from the request path
	 * @param model model map used to expose owner and pet data to the view
	 * @return a new visit associated with the selected pet
	 * @throws IllegalArgumentException if the owner or pet cannot be found
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
			Map<String, Object> model) {
		Optional<Owner> optionalOwner = owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new IllegalArgumentException(
					"Pet with id " + petId + " not found for owner with id " + ownerId + ".");
		}
		model.put("pet", pet);
		model.put("owner", owner);

		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}

	/**
	 * Shows the form used to book a new visit for a pet.
	 *
	 * <p>
	 * Spring MVC calls {@link #loadPetWithVisit(int, int, Map)} before this handler so
	 * the view has access to the relevant owner, pet, and visit objects.
	 * @return the visit creation form view
	 */
	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String initNewVisitForm() {
		return "pets/createOrUpdateVisitForm";
	}

	/**
	 * Processes the submitted form for booking a new pet visit.
	 *
	 * <p>
	 * If validation fails, the creation form is shown again. Otherwise, the visit is
	 * added to the selected pet and the owner record is saved.
	 * @param owner owner loaded for the current request
	 * @param petId identifier of the pet receiving the visit
	 * @param visit visit data submitted from the form
	 * @param result validation result for the submitted visit
	 * @param redirectAttributes flash attributes shown after a successful redirect
	 * @return the form view when validation fails, or a redirect to the owner details
	 * page
	 */
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Visit visit,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}

		owner.addVisit(petId, visit);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Your visit has been booked");
		return "redirect:/owners/{ownerId}";
	}

}
