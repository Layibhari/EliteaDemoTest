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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles owner-related web requests and coordinates owner lookup, creation, update, and
 * display flows.
 *
 * <p>
 * The current PetClinic codebase does not contain a dedicated service package under
 * {@code src/main/java}. This controller is documented as a key service-facing request
 * flow class because it coordinates validation, repository access, pagination, and
 * redirects for owner management.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Controller
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	/**
	 * Creates an owner controller using the repository responsible for owner data.
	 * @param owners repository used to find and save owners
	 */
	public OwnerController(OwnerRepository owners) {
		this.owners = owners;
	}

	/**
	 * Prevents request parameters from directly binding owner identifiers.
	 * @param dataBinder binder used to configure restricted fields
	 */
	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Provides the owner model attribute used by owner request handlers.
	 *
	 * <p>
	 * If an owner identifier is available in the request path, the existing owner is
	 * loaded. Otherwise, a new owner is created for form binding.
	 * @param ownerId optional owner identifier from the request path
	 * @return the matching owner, or a new owner for creation forms
	 */
	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner()
				: this.owners.findById(ownerId)
					.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
							+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	/**
	 * Shows the form for creating a new owner.
	 * @return the owner creation form view
	 */
	@GetMapping("/owners/new")
	public String initCreationForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes a submitted owner creation form.
	 * @param owner owner data submitted by the user
	 * @param result validation result for the submitted owner
	 * @param redirectAttributes flash attributes used after validation errors or
	 * redirects
	 * @return the creation form when validation fails, or a redirect to the new owner
	 * page
	 */
	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		return "redirect:/owners/" + owner.getId();
	}

	/**
	 * Shows the form for searching owners.
	 * @return the owner search form view
	 */
	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	/**
	 * Handles owner search requests and displays matching results.
	 *
	 * <p>
	 * A request without a last-name value returns all owners. A single matching owner
	 * redirects to the owner details page, while multiple matches are displayed using
	 * pagination.
	 * @param page page number to display, starting from 1
	 * @param owner owner search criteria bound from request parameters
	 * @param result binding result used to report search errors
	 * @param model model used to expose paginated owner results
	 * @return the search form, owner details redirect, or owner list view
	 */
	@GetMapping("/owners")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model) {
		// allow parameterless GET request for /owners to return all records
		String lastName = owner.getLastName();
		if (lastName == null) {
			lastName = ""; // empty string signifies broadest possible search
		}

		// find owners by last name
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			owner = ownersResults.iterator().next();
			return "redirect:/owners/" + owner.getId();
		}

		// multiple owners found
		return addPaginationModel(page, model, ownersResults);
	}

	/**
	 * Adds pagination information and owner results to the model.
	 * @param page current page number
	 * @param model model used by the owner list view
	 * @param paginated paginated owner results
	 * @return the owner list view
	 */
	private String addPaginationModel(int page, Model model, Page<Owner> paginated) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	/**
	 * Finds owners whose last names start with the supplied search text.
	 * @param page page number to retrieve, starting from 1
	 * @param lastname last-name prefix used for searching owners
	 * @return a page of matching owners
	 */
	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	/**
	 * Shows the form for updating an existing owner.
	 * @return the owner update form view
	 */
	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes a submitted owner update form.
	 *
	 * <p>
	 * The path identifier is checked against the submitted owner identifier before saving
	 * to avoid updating the wrong owner record.
	 * @param owner owner data submitted by the user
	 * @param result validation result for the submitted owner
	 * @param ownerId owner identifier from the request path
	 * @param redirectAttributes flash attributes used after validation errors or
	 * redirects
	 * @return the update form when validation fails, or a redirect to the owner details
	 * page
	 */
	@PostMapping("/owners/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		if (!Objects.equals(owner.getId(), ownerId)) {
			result.rejectValue("id", "mismatch", "The owner ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Owner ID mismatch. Please try again.");
			return "redirect:/owners/{ownerId}/edit";
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		mav.addObject(owner);
		return mav;
	}

}
