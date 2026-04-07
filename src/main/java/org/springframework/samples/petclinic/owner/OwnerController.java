package org.springframework.samples.petclinic.owner;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	public OwnerController(OwnerRepository owners) {
		this.owners = owners;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId + "."));
	}

	// =================== CREACIÓN ===================
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
		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		return "redirect:/owners/" + owner.getId();
	}

	// =================== BÚSQUEDA ===================
	@GetMapping("/owners/find")
	public String initFindForm(Model model) {
		model.addAttribute("owner", new Owner());
		return "owners/findOwners";
	}

	@GetMapping("/owners")
	public String processFindForm(Owner owner, BindingResult result, @RequestParam(defaultValue = "1") int page,
			Model model) {
		String lastName = owner.getLastName();
		if (lastName == null)
			lastName = "";

		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);

		if (ownersResults.isEmpty()) {
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			Owner singleOwner = ownersResults.iterator().next();
			return "redirect:/owners/" + singleOwner.getId();
		}

		return addPaginationModel(page, model, ownersResults);
	}

	// Nuevo endpoint para buscar por apellido (parámetro opcional)
	@GetMapping("/owners/searchByLastName")
	public String searchByLastName(@RequestParam(name = "lastName", required = false) String lastName,
			@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result, Model model) {
		if (lastName == null)
			lastName = "";
		owner.setLastName(lastName);

		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);

		if (ownersResults.isEmpty()) {
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			Owner singleOwner = ownersResults.iterator().next();
			return "redirect:/owners/" + singleOwner.getId();
		}

		return addPaginationModel(page, model, ownersResults);
	}

	// Nuevo endpoint para mostrar todos los propietarios
	@GetMapping("/owners/showAll")
	public String showAllOwners(@RequestParam(defaultValue = "1") int page, Model model) {
		Pageable pageable = PageRequest.of(page - 1, 5);
		Page<Owner> ownersResults = owners.findAll(pageable);
		return addPaginationModel(page, model, ownersResults);
	}

	// =================== UTILIDADES ===================
	private String addPaginationModel(int page, Model model, Page<Owner> paginated) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		Pageable pageable = PageRequest.of(page - 1, 5);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	// =================== EDICIÓN ===================
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
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	// =================== DETALLE ===================
	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		Owner owner = this.owners.findById(ownerId)
			.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId));
		mav.addObject(owner);
		return mav;
	}

}
