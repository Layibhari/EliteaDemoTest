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

package org.springframework.samples.petclinic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WebIntegrationTests {

	@LocalServerPort
	int port;

	@Autowired
	private RestTemplateBuilder builder;

	@Test
	void welcomePageShouldReturnWelcomeView() {
		RestTemplate restTemplate = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Welcome");
	}

	@Test
	void ownerFindPageShouldRenderSearchForm() {
		RestTemplate restTemplate = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<String> response = restTemplate.getForEntity("/owners/find", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Find Owners");
	}

	@Test
	void ownersListShouldReturnOwnersPage() {
		RestTemplate restTemplate = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<String> response = restTemplate.getForEntity("/owners?lastName=", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Owners");
	}

	@Test
	void ownerDetailsShouldReturnExistingOwner() {
		RestTemplate restTemplate = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<String> response = restTemplate.getForEntity("/owners/1", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Owner Information");
	}

	@Test
	void vetsEndpointShouldReturnJsonList() {
		RestTemplate restTemplate = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<Map<String, List<Map<String, Object>>>> response = restTemplate.exchange(
				RequestEntity.get("/vets").build(),
				new ParameterizedTypeReference<>() {
				});

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsKey("vetList");
		assertThat(response.getBody().get("vetList")).isNotEmpty();
	}

	@Test
	void waitingCountEndpointShouldReturnNumericValue() {
		RestTemplate restTemplate = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<Map<String, Integer>> response = restTemplate.exchange(
				RequestEntity.get("/vets/waiting-count").build(),
				new ParameterizedTypeReference<>() {
				});

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsKey("waitingCount");
		assertThat(response.getBody().get("waitingCount")).isGreaterThanOrEqualTo(0);
	}

}
