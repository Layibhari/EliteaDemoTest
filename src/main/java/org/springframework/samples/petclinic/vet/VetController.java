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
package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Web Controller managing veterinarian list pages and API endpoints.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	/**
	 * Repository providing database operations for vets.
	 */
	private final VetRepository vetRepository;

	/**
	 * Constructor injecting the vet repository dependency.
	 * @param vetRepository VetRepository dependency
	 */
	public VetController(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	/**
	 * Handles HTML request to render the list of veterinarians with pagination.
	 * @param page requested page number index (defaults to 1)
	 * @param model UI model context container
	 * @return Thymeleaf template name
	 */
	@GetMapping("/vets.html")
	public String showVetList(@RequestParam(defaultValue = "1") int page, Model model) {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for Object-Xml mapping (JAXB marshalling)
		Vets vets = new Vets();
		Page<Vet> paginated = findPaginated(page);
		vets.getVetList().addAll(paginated.toList());
		return addPaginationModel(page, paginated, model);
	}

	/**
	 * Helper utility to append pagination numbers, total records, and content to MVC UI
	 * context.
	 * @param page current active page page index
	 * @param paginated active JPA page object results
	 * @param model view model context container
	 * @return Thymeleaf view template name
	 */
	private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	/**
	 * Paginate lookup helper for Vet entities.
	 * @param page page number index (1-indexed base)
	 * @return JPA page response
	 */
	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		// Pageable index is 0-indexed in Spring Data JPA, thus subtract 1 from page index
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vetRepository.findAll(pageable);
	}

	/**
	 * API Endpoint rendering the list of vets in XML/JSON format. Annotated
	 * with @ResponseBody to serialize response content directly to HTTP body.
	 * @return Vets container holding serialized veterinarian objects
	 */
	@GetMapping({ "/vets" })
	public @ResponseBody Vets showResourcesVetList() {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for JSon/Object mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetRepository.findAll());
		return vets;
	}

}
