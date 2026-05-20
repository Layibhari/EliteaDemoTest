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

import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Tests for {@link Owner} bean validation constraints.
 */
class OwnerValidationTests {

	private Validator validator;

	@BeforeEach
	void setUp() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);
		LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
		factory.afterPropertiesSet();
		validator = factory;
	}

	private Owner validOwner() {
		Owner owner = new Owner();
		owner.setFirstName("John");
		owner.setLastName("Doe");
		owner.setAddress("123 Main Street");
		owner.setCity("Springfield");
		owner.setTelephone("1234567890");
		return owner;
	}

	@Test
	void validOwnerHasNoViolations() {
		Set<ConstraintViolation<Owner>> violations = validator.validate(validOwner());
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldNotValidateWhenAddressBlank() {
		Owner owner = validOwner();
		owner.setAddress("");
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath()).hasToString("address");
	}

	@Test
	void shouldNotValidateWhenCityBlank() {
		Owner owner = validOwner();
		owner.setCity("");
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath()).hasToString("city");
	}

	@Test
	void shouldNotValidateWhenTelephoneBlank() {
		Owner owner = validOwner();
		owner.setTelephone("");
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("telephone");
	}

	@Test
	void shouldNotValidateWhenTelephoneTooShort() {
		Owner owner = validOwner();
		owner.setTelephone("12345");
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath()).hasToString("telephone");
	}

	@Test
	void shouldNotValidateWhenTelephoneContainsLetters() {
		Owner owner = validOwner();
		owner.setTelephone("123456789A");
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath()).hasToString("telephone");
	}

	@Test
	void shouldNotValidateWhenFirstNameBlank() {
		Owner owner = validOwner();
		owner.setFirstName("");
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath()).hasToString("firstName");
	}

	@Test
	void shouldNotValidateWhenLastNameBlank() {
		Owner owner = validOwner();
		owner.setLastName("");
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath()).hasToString("lastName");
	}

	@Test
	void telephoneMustBeExactlyTenDigits() {
		Owner owner = validOwner();
		owner.setTelephone("12345678901");
		Set<ConstraintViolation<Owner>> violations = validator.validate(owner);
		assertThat(violations).hasSize(1);
		assertThat(violations.iterator().next().getPropertyPath()).hasToString("telephone");
	}

}
