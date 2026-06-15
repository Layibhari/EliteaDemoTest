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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller binding integration coverage for reviewed Owner scenarios.
 */
@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerControllerBindingTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@Test
	void int007OwnerUpdateIgnoresMassAssignmentAttemptsForIds() throws Exception {
		Owner owner = ownerWithPet(1, 7);
		given(this.owners.findById(1)).willReturn(Optional.of(owner));

		this.mockMvc
			.perform(post("/owners/{ownerId}/edit", 1).param("id", "99")
				.param("pets[0].id", "88")
				.param("firstName", "Changed")
				.param("lastName", "Owner")
				.param("address", "500 Changed Street")
				.param("city", "Middleton")
				.param("telephone", "6085551111"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));

		ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
		verify(this.owners).save(captor.capture());
		Owner saved = captor.getValue();
		assertThat(saved.getId()).isEqualTo(1);
		assertThat(saved.getPets().get(0).getId()).isEqualTo(7);
		assertThat(saved.getFirstName()).isEqualTo("Changed");
		assertThat(saved.getAddress()).isEqualTo("500 Changed Street");
	}

	@Test
	void int008MismatchedOwnerModelIdDoesNotSaveAndRedirectsBackToEdit() throws Exception {
		Owner owner = ownerWithPet(2, 7);
		given(this.owners.findById(1)).willReturn(Optional.of(owner));

		this.mockMvc.perform(post("/owners/{ownerId}/edit", 1).flashAttr("owner", owner))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/1/edit"))
			.andExpect(flash().attributeExists("error"));

		verify(this.owners, never()).save(any(Owner.class));
	}

	private Owner ownerWithPet(int ownerId, int petId) {
		Owner owner = new Owner();
		owner.setId(ownerId);
		owner.setFirstName("Original");
		owner.setLastName("Owner");
		owner.setAddress("100 Original Street");
		owner.setCity("Madison");
		owner.setTelephone("6085550000");
		Pet pet = new Pet();
		pet.setId(petId);
		pet.setName("Max");
		owner.getPets().add(pet);
		return owner;
	}

}
