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
 * Web Controller handling owner-related requests.
 *
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Controller
class OwnerController {

	/**
	 * Relative Thymeleaf view path to render owner creation or modification forms.
	 */
	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	/**
	 * Repository providing database operations for owners.
	 */
	private final OwnerRepository owners;

	/**
	 * Constructs the controller injecting the owner repository.
	 * @param owners OwnerRepository dependency
	 */
	public OwnerController(OwnerRepository owners) {
		this.owners = owners;
	}

	/**
	 * Configures custom request binding behavior. In particular, disallows direct binding
	 * of the "id" parameter to protect against tampering attacks.
	 * @param dataBinder Spring's request parameter binder
	 */
	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		// Prevent user from setting/modifying 'id' field in request data binding
		dataBinder.setDisallowedFields("id", "*.id");
	}

	/**
	 * Helper model attribute method that automatically fetches the owner by path variable
	 * "ownerId". If no "ownerId" is present, initializes and returns a blank new Owner
	 * instance.
	 * @param ownerId optional owner ID extracted from URI path
	 * @return Owner object corresponding to ID, or new Owner
	 * @throws IllegalArgumentException if the ID is specified but not found in repository
	 */
	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		// If ID is not present, return a new transient owner object, otherwise retrieve
		// it from DB
		return ownerId == null ? new Owner()
				: this.owners.findById(ownerId)
					.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
							+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	/**
	 * Initializes the form for creating a new Owner.
	 * @return Thymeleaf template name for the owner form
	 */
	@GetMapping("/owners/new")
	public String initCreationForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes the submission of a new Owner creation form.
	 * @param owner validation-validated owner model attribute
	 * @param result holds validation errors if any
	 * @param redirectAttributes utility for passing flash attributes across redirects
	 * @return view redirect on success, or stays on form page if errors exist
	 */
	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
		// If input validation fails, set validation error attribute and redisplay the
		// form
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		// Save new owner instance to database
		this.owners.save(owner);
		// Add success alert flash message
		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		// Redirect to detail page of newly created owner
		return "redirect:/owners/" + owner.getId();
	}

	/**
	 * Initializes the find owner search form view.
	 * @return Thymeleaf search template name
	 */
	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	/**
	 * Processes the lookup search request by owner last name. Supports pagination and
	 * navigates dynamically based on match counts.
	 * @param page requested page index (defaults to page 1)
	 * @param owner search criteria model mapping owner fields
	 * @param result validation registry to signal invalid matches
	 * @param model Spring MVC view model mapping page attributes
	 * @return view name redirecting to single owner or list of owners
	 */
	@GetMapping("/owners")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model) {
		// allow parameterless GET request for /owners to return all records
		String lastName = owner.getLastName();
		if (lastName == null) {
			lastName = ""; // empty string signifies broadest possible search (returns all
							// owners)
		}

		// find owners by last name starting with criteria, paginating findings
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (ownersResults.isEmpty()) {
			// no owners found - reject value and present form again
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			// exactly 1 owner found - skip selection list and redirect directly to owner
			// details
			owner = ownersResults.iterator().next();
			return "redirect:/owners/" + owner.getId();
		}

		// multiple owners found - append pagination controls and render summary list
		return addPaginationModel(page, model, ownersResults);
	}

	/**
	 * Helper utility to append pagination numbers, total records, and content to MVC UI
	 * context.
	 * @param page current active page page index
	 * @param model view model context container
	 * @param paginated active JPA page object results
	 * @return summary template list view
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
	 * Paginate lookup helper for Owner entity.
	 * @param page page number index (1-indexed base)
	 * @param lastname starting criteria
	 * @return JPA page response
	 */
	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		// Pageable index is 0-indexed in Spring Data JPA, thus subtract 1 from page index
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	/**
	 * Initializes update/edit owner form.
	 * @return form view path
	 */
	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes post form edits for owner.
	 * @param owner validated form model attributes
	 * @param result binding validation rules check
	 * @param ownerId URL path parameter owner ID
	 * @param redirectAttributes redirect attributes container
	 * @return details redirect or form edit view
	 */
	@PostMapping("/owners/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId,
			RedirectAttributes redirectAttributes) {
		// Validation check
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		// Check for ID mismatch between path URL parameter and request payload object
		if (!Objects.equals(owner.getId(), ownerId)) {
			result.rejectValue("id", "mismatch", "The owner ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Owner ID mismatch. Please try again.");
			return "redirect:/owners/{ownerId}/edit";
		}

		// Assign the matched ID and update record details in repository
		owner.setId(ownerId);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelAndView wrapper holding detail view configuration and attributes
	 */
	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		// Lookup owner or raise exception if not found
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		mav.addObject(owner);
		return mav;
	}

}
