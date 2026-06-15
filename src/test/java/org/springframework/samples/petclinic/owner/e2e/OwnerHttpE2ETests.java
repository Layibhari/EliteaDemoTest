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
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.boot.http.client.HttpRedirects;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * API E2E coverage for reviewed Owner scenarios.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
@DisabledInNativeImage
class OwnerHttpE2ETests {

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private TestRestTemplate rest;

	@Autowired
	private OwnerRepository owners;

	private final List<String> createdLastNames = new ArrayList<>();

	@AfterEach
	void cleanUpCreatedOwners() {
		for (String lastName : this.createdLastNames) {
			List<Owner> toDelete = this.owners.findByLastNameStartingWith(lastName, Pageable.unpaged())
				.stream()
				.filter(owner -> lastName.equals(owner.getLastName()))
				.toList();
			this.owners.deleteAll(toDelete);
		}
		this.createdLastNames.clear();
	}

	@Test
	void e2e001FindOwnersPageOpensWithEmptySearchForm() {
		ResponseEntity<String> response = get("/owners/find");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body(response)).contains("Find Owners", "id=\"search-owner-form\"", "name=\"lastName\"",
				"Add Owner");
	}

	@Test
	void e2e003LastNamePrefixWithMultipleOwnersRendersListPage() {
		ResponseEntity<String> response = get("/owners?lastName=Davis&page=1");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body(response)).contains("Betty Davis", "638 Cardinal Ave.", "Sun Prairie", "6085551749", "Basil",
				"Harold Davis", "563 Friendly St.", "Windsor", "6085553198", "Iggy");
	}

	@Test
	void e2e004SingleOwnerSearchRedirectsToOwnerDetails() {
		ResponseEntity<String> response = getNoRedirect("/owners?lastName=Franklin&page=1");

		assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
		assertThat(response.getHeaders().getLocation()).isNotNull();
		assertThat(response.getHeaders().getLocation().toString()).endsWith("/owners/1");

		ResponseEntity<String> details = get(response.getHeaders().getLocation().toString());
		assertThat(details.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body(details)).contains("George Franklin", "110 W. Liberty St.", "Madison", "6085551023");
	}

	@Test
	void e2e005UnknownLastNameSearchReturnsFindFormWithNotFoundMessage() {
		ResponseEntity<String> response = get("/owners?lastName=Unknown%20Surname&page=1");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body(response)).contains("Find Owners", "has not been found").doesNotContain("id=\"owners\"");
	}

	@Test
	void e2e006ValidOwnerCreationPersistsAndRedirectsWithSuccessMessage() {
		String lastName = uniqueLastName("E2E006");
		this.createdLastNames.add(lastName);

		ResponseEntity<String> response = postForm("/owners/new",
				ownerForm("Automation", lastName, "123 Test Street", "Madison", "6085550001"));

		assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
		URI location = response.getHeaders().getLocation();
		assertThat(location).isNotNull();
		assertThat(location.toString()).contains("/owners/");

		ResponseEntity<String> details = get(location.toString(), cookies(response));
		assertThat(details.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body(details)).contains("New Owner Created", "Automation " + lastName, "123 Test Street", "Madison",
				"6085550001");
	}

	@Test
	void e2e007InvalidOwnerCreationReturnsFormAndDoesNotCreateRow() {
		String lastName = uniqueLastName("E2E007");

		ResponseEntity<String> response = postForm("/owners/new",
				ownerForm("Automation", lastName, "", "Madison", "not-valid"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body(response)).contains("Owner", "has-error", "Telephone must be a 10-digit number");
		assertThat(this.owners.findByLastNameStartingWith(lastName, Pageable.unpaged())).isEmpty();
	}

	@Test
	void e2e008OwnerDetailsShowsContactPetsVisitsAndActions() {
		ResponseEntity<String> response = get("/owners/6");

		String page = body(response);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(page).contains("Jean Coleman", "105 N. Lake St.", "Monona", "6085552654", "Pets and Visits",
				"href=\"6/edit\"", "href=\"6/pets/new\"", "href=\"6/pets/8/edit\"", "href=\"6/pets/8/visits/new\"",
				"href=\"6/pets/7/edit\"", "href=\"6/pets/7/visits/new\"");
		assertThat(page.indexOf("Max")).isLessThan(page.indexOf("Samantha"));
		assertThat(page).containsSubsequence("Max", "2012-09-04", "cat", "2013-01-02", "rabies shot", "2013-01-03",
				"neutered");
		assertThat(page).containsSubsequence("Samantha", "2012-09-04", "cat", "2013-01-01", "rabies shot", "2013-01-04",
				"spayed");
	}

	@Test
	void e2e009ValidOwnerUpdatePersistsChangedFieldsAndRedirectsWithSuccessMessage() {
		Owner owner = new Owner();
		owner.setFirstName("Update");
		owner.setLastName(uniqueLastName("E2E009"));
		owner.setAddress("123 Old Street");
		owner.setCity("Madison");
		owner.setTelephone("6085550002");
		this.createdLastNames.add(owner.getLastName());
		this.owners.save(owner);

		ResponseEntity<String> response = postForm("/owners/" + owner.getId() + "/edit",
				ownerForm("Update", owner.getLastName(), "456 New Avenue", "Monona", "6085550003"));

		assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
		assertThat(response.getHeaders().getLocation()).isNotNull();
		assertThat(response.getHeaders().getLocation().toString()).contains("/owners/" + owner.getId());

		ResponseEntity<String> details = get(response.getHeaders().getLocation().toString(), cookies(response));
		assertThat(details.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(body(details)).contains("Owner Values Updated", "Update " + owner.getLastName(), "456 New Avenue",
				"Monona", "6085550003");
	}

	@Test
	void e2e010UnknownOwnerDetailReturnsApplicationErrorResponse() {
		ResponseEntity<String> response = get("/owners/999999");

		assertThat(response.getStatusCode().is5xxServerError()).isTrue();
		assertThat(body(response)).doesNotContain("Owner Information", "George Franklin");
	}

	private ResponseEntity<String> get(String path) {
		return get(path, List.of());
	}

	private ResponseEntity<String> get(String path, List<String> cookies) {
		return this.rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(htmlHeaders(cookies)), String.class);
	}

	private ResponseEntity<String> getNoRedirect(String path) {
		return this.rest.withRedirects(HttpRedirects.DONT_FOLLOW)
			.exchange(url(path), HttpMethod.GET, new HttpEntity<>(htmlHeaders(List.of())), String.class);
	}

	private ResponseEntity<String> postForm(String path, MultiValueMap<String, String> form) {
		HttpHeaders headers = htmlHeaders(List.of());
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		return this.rest.withRedirects(HttpRedirects.DONT_FOLLOW)
			.exchange(url(path), HttpMethod.POST, new HttpEntity<>(form, headers), String.class);
	}

	private HttpHeaders htmlHeaders(List<String> cookies) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.TEXT_HTML));
		if (!cookies.isEmpty()) {
			headers.put(HttpHeaders.COOKIE, cookies);
		}
		return headers;
	}

	private List<String> cookies(ResponseEntity<?> response) {
		return response.getHeaders()
			.getOrEmpty(HttpHeaders.SET_COOKIE)
			.stream()
			.map(header -> header.split(";", 2)[0])
			.toList();
	}

	private String url(String path) {
		if (path.startsWith("http://") || path.startsWith("https://")) {
			return path;
		}
		return "http://localhost:" + this.port + path;
	}

	private MultiValueMap<String, String> ownerForm(String firstName, String lastName, String address, String city,
			String telephone) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("firstName", firstName);
		form.add("lastName", lastName);
		form.add("address", address);
		form.add("city", city);
		form.add("telephone", telephone);
		return form;
	}

	private String uniqueLastName(String scenarioId) {
		return "Auto" + scenarioId + Long.toString(System.nanoTime(), 36);
	}

	private String body(ResponseEntity<String> response) {
		assertThat(response.getBody()).isNotNull();
		return response.getBody();
	}

}
