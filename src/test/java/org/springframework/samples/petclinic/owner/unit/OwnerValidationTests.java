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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.Validator;

/**
 * Unit-style validation coverage for reviewed Owner scenarios.
 */
class OwnerValidationTests {

	private final Validator validator = createValidator();

	@Test
	void unit008TelephoneAcceptsExactlyTenDigitsAndRejectsOtherFormats() {
		assertThat(hasTelephoneViolation("0123456789")).isFalse();
		assertThat(List.of("012-345-6789", "abcdefghij", "12345")).allSatisfy(
				telephone -> assertThat(hasTelephoneViolation(telephone)).as("telephone %s", telephone).isTrue());
	}

	private boolean hasTelephoneViolation(String telephone) {
		Owner owner = validOwner();
		owner.setTelephone(telephone);
		return this.validator.validate(owner)
			.stream()
			.anyMatch(violation -> violation.getPropertyPath().toString().equals("telephone"));
	}

	private Owner validOwner() {
		Owner owner = new Owner();
		owner.setFirstName("Valid");
		owner.setLastName("Owner");
		owner.setAddress("123 Test Street");
		owner.setCity("Madison");
		owner.setTelephone("0123456789");
		return owner;
	}

	private Validator createValidator() {
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		localValidatorFactoryBean.afterPropertiesSet();
		return localValidatorFactoryBean;
	}

}
