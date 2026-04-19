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
 * @author Spring PetClinic
 */
@Controller
class VaccineController {

	private final OwnerRepository owners;

	public VaccineController(OwnerRepository owners) {
		this.owners = owners;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("vaccine")
	public Vaccine loadPetWithVaccine(@PathVariable("ownerId") int ownerId, @PathVariable("petId") int petId,
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

		Vaccine vaccine = new Vaccine();
		pet.addVaccine(vaccine);
		return vaccine;
	}

	// Spring MVC calls method loadPetWithVaccine(...) before initNewVaccineForm is
	// called
	@GetMapping("/owners/{ownerId}/pets/{petId}/vaccines/new")
	public String initNewVaccineForm() {
		return "pets/createOrUpdateVaccineForm";
	}

	// Spring MVC calls method loadPetWithVaccine(...) before processNewVaccineForm is
	// called
	@PostMapping("/owners/{ownerId}/pets/{petId}/vaccines/new")
	public String processNewVaccineForm(@ModelAttribute Owner owner, @PathVariable int petId, @Valid Vaccine vaccine,
			BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return "pets/createOrUpdateVaccineForm";
		}

		owner.addVaccine(petId, vaccine);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Vaccine record has been added");
		return "redirect:/owners/{ownerId}";
	}

}
