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

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;

/**
 * Bean Validation integration coverage for reviewed Owner scenarios.
 */
class OwnerValidationIntegrationTests {

	private final Validator validator = createValidator();

	@Test
	void int006OwnerBeanValidationRejectsRequiredFieldsAndInvalidTelephone() {
		Owner owner = new Owner();
		owner.setFirstName("");
		owner.setLastName("");
		owner.setAddress("");
		owner.setCity("");
		owner.setTelephone("12345");

		Set<String> violationPaths = this.validator.validate(owner)
			.stream()
			.map(violation -> violation.getPropertyPath().toString())
			.collect(Collectors.toSet());

		assertThat(violationPaths).containsExactlyInAnyOrder("firstName", "lastName", "address", "city", "telephone");
	}

	private Validator createValidator() {
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean.afterPropertiesSet();
		return localValidatorFactoryBean;
	}

}
