/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
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
	private final OwnerTrie ownerTrie; // In-memory Trie for fast partial searches

	public OwnerController(OwnerRepository owners) {
		this.owners = owners;
		this.ownerTrie = new OwnerTrie();
	}

	/**
	 * Populates the Trie on application startup.
	 * Note: Requires a findAll() method in OwnerRepository.
	 * If you don't have one, you can add `Iterable<Owner> findAll();` to the interface.
	 */
	@PostConstruct
	public void initTrie() {
		// Populate the Trie with all existing owners from the database
		for (Owner owner : this.owners.findAll()) {
			this.ownerTrie.insert(owner);
		}
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner()
				: this.owners.findById(ownerId)
					.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
							+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	@GetMapping("/owners/new")
	public String initCreationForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		this.ownerTrie.insert(owner); // Add to Trie upon creation

		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

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

	// ---------------------------------------------------------
	// NEW: Trie-based Search Endpoint
	// ---------------------------------------------------------
	@GetMapping("/owners/trieSearch")
	public String searchUsingTrie(@RequestParam("lastName") String partialName, Model model) {
		List<Owner> matches = ownerTrie.searchByPrefix(partialName);

		if (matches.isEmpty()) {
			model.addAttribute("error", "No owners found matching that prefix.");
			return "owners/findOwners";
		}

		if (matches.size() == 1) {
			return "redirect:/owners/" + matches.get(0).getId();
		}

		model.addAttribute("listOwners", matches);
		// Note: A real Trie doesn't natively support Spring Data pagination without custom wrappers.
		// For simplicity, we pass the raw list to the view.
		return "owners/ownersList";
	}
	// ---------------------------------------------------------

	private String addPaginationModel(int page, Model model, Page<Owner> paginated) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

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
		this.ownerTrie.insert(owner); // Update the Trie.

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

	// ---------------------------------------------------------
	// NEW: Trie Data Structure Implementations
	// ---------------------------------------------------------
	private static class TrieNode {
		Map<Character, TrieNode> children = new HashMap<>();
		List<Owner> owners = new ArrayList<>(); // Store owners that end at or pass through this node
	}

	private static class OwnerTrie {
		private final TrieNode root = new TrieNode();

		public void insert(Owner owner) {
			String lastName = owner.getLastName();
			if (lastName == null || lastName.trim().isEmpty()) {
				return;
			}

			TrieNode current = root;
			for (char c : lastName.toLowerCase().toCharArray()) {
				current = current.children.computeIfAbsent(c, k -> new TrieNode());
			}

			// Prevent duplicate references if updating an existing owner
			boolean exists = current.owners.stream().anyMatch(o -> o.getId().equals(owner.getId()));
			if (!exists) {
				current.owners.add(owner);
			} else {
				// Replace the old owner object with the newly updated one
				current.owners.removeIf(o -> o.getId().equals(owner.getId()));
				current.owners.add(owner);
			}
		}

		public List<Owner> searchByPrefix(String prefix) {
			if (prefix == null || prefix.trim().isEmpty()) {
				return Collections.emptyList();
			}

			TrieNode current = root;
			for (char c : prefix.toLowerCase().toCharArray()) {
				current = current.children.get(c);
				if (current == null) {
					return Collections.emptyList(); // Prefix not found
				}
			}
			return collectAllOwners(current);
		}

		// Recursively gather all owners stored under a specific prefix node
		private List<Owner> collectAllOwners(TrieNode node) {
			List<Owner> results = new ArrayList<>(node.owners);
			for (TrieNode child : node.children.values()) {
				results.addAll(collectAllOwners(child));
			}
			return results;
		}
	}
}
