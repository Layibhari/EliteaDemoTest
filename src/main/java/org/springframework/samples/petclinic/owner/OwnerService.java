package org.springframework.samples.petclinic.owner;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@Service
public class OwnerService {

	private final OwnerRepository owners;

	public OwnerService(OwnerRepository owners) {
		this.owners = owners;
	}

	public String addPaginationModel(int page, Model model, Page<Owner> paginated) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	public Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	public Page<Owner> findPaginatedForOwnersEmail(int page, String email) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return  owners.findByEmailStartingWith(email, pageable);
	}

	public Owner findOwnerById(Integer ownerId){
		return owners.findById(ownerId).orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
			+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	public Optional<Owner> findOptionalOwnerById(Integer id) {
		return owners.findById(id);
	}

	public Owner save(Owner owner){
		return owners.save(owner);
	}

}
