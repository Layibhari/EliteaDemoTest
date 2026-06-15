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

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Owner repository integration coverage for reviewed scenarios.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OwnerRepositoryIntegrationTests {

	@Autowired
	private OwnerRepository owners;

	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void int002BlankLastNameSearchSupportsDeterministicPagination() {
		Page<Owner> firstPage = this.owners.findByLastNameStartingWith("", PageRequest.of(0, 5));
		Page<Owner> secondPage = this.owners.findByLastNameStartingWith("", PageRequest.of(1, 5));

		assertThat(firstPage.getContent()).hasSize(5);
		assertThat(secondPage.getContent()).hasSize(5);
		assertThat(firstPage.getTotalElements()).isEqualTo(10);
		assertThat(secondPage.getTotalElements()).isEqualTo(10);
		assertThat(firstPage.getContent()).extracting(Owner::getId)
			.doesNotContainAnyElementsOf(secondPage.getContent().stream().map(Owner::getId).toList());
	}

	@Test
	void int003SavingNewOwnerGeneratesIdAndPersistsContactFields() {
		Owner owner = new Owner();
		owner.setFirstName("Sam");
		owner.setLastName("Roundtrip");
		owner.setAddress("4 Evans Street");
		owner.setCity("Wollongong");
		owner.setTelephone("4444444444");

		Owner saved = this.owners.save(owner);
		this.entityManager.flush();
		this.entityManager.clear();

		Owner reloaded = this.owners.findById(saved.getId()).orElseThrow();
		assertThat(reloaded.getId()).isNotNull();
		assertThat(reloaded.getFirstName()).isEqualTo("Sam");
		assertThat(reloaded.getLastName()).isEqualTo("Roundtrip");
		assertThat(reloaded.getAddress()).isEqualTo("4 Evans Street");
		assertThat(reloaded.getCity()).isEqualTo("Wollongong");
		assertThat(reloaded.getTelephone()).isEqualTo("4444444444");
	}

	@Test
	void int004UpdatingOwnerContactFieldsPreservesExistingPetRelationships() {
		Owner owner = this.owners.findById(6).orElseThrow();
		List<Integer> petIds = owner.getPets().stream().map(Pet::getId).toList();

		owner.setAddress("400 Updated Lane");
		owner.setCity("Middleton");
		owner.setTelephone("6085559999");
		this.owners.save(owner);
		this.entityManager.flush();
		this.entityManager.clear();

		Owner reloaded = this.owners.findById(6).orElseThrow();
		assertThat(reloaded.getAddress()).isEqualTo("400 Updated Lane");
		assertThat(reloaded.getCity()).isEqualTo("Middleton");
		assertThat(reloaded.getTelephone()).isEqualTo("6085559999");
		assertThat(reloaded.getPets()).extracting(Pet::getId).containsExactlyElementsOf(petIds);
	}

	@Test
	void int005ReloadedOwnerAggregateContainsPetsAndVisitsInExpectedOrder() {
		Owner owner = this.owners.findById(6).orElseThrow();

		assertThat(owner.getPets()).extracting(Pet::getName).containsExactly("Max", "Samantha");
		Pet max = owner.getPet("Max");
		Pet samantha = owner.getPet("Samantha");
		assertThat(max.getVisits()).extracting(Visit::getDate)
			.containsExactly(LocalDate.of(2013, 1, 2), LocalDate.of(2013, 1, 3));
		assertThat(samantha.getVisits()).extracting(Visit::getDate)
			.containsExactly(LocalDate.of(2013, 1, 1), LocalDate.of(2013, 1, 4));
	}

}
