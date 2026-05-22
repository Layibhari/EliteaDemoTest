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

package org.springframework.samples.petclinic.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Visit;

/**
 * @author Michael Isvy Simple test to make sure that Bean Validation is working (useful
 * when upgrading to a new version of Hibernate Validator/ Bean Validation)
 */
class ValidatorTests {

	private Validator createValidator() {
		LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("messages/messages");
		localValidatorFactoryBean.setValidationMessageSource(messageSource);
		localValidatorFactoryBean.afterPropertiesSet();
		return localValidatorFactoryBean;
	}

	private <T> ConstraintViolation<T> getOnlyViolation(Set<ConstraintViolation<T>> violations) {
		assertThat(violations).hasSize(1);
		return violations.iterator().next();
	}

	@Test
	void shouldNotValidateWhenFirstNameEmpty() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Person person = new Person();
		person.setFirstName("");
		person.setLastName("smith");

		Validator validator = createValidator();
		Set<ConstraintViolation<Person>> constraintViolations = validator.validate(person);

		ConstraintViolation<Person> violation = getOnlyViolation(constraintViolations);
		assertThat(violation.getPropertyPath()).hasToString("firstName");
		assertThat(violation.getMessage()).isEqualTo("must not be blank");
	}

	@Test
	void shouldNotValidateWhenLastNameEmpty() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Person person = new Person();
		person.setFirstName("George");
		person.setLastName("");

		Validator validator = createValidator();
		Set<ConstraintViolation<Person>> constraintViolations = validator.validate(person);

		ConstraintViolation<Person> violation = getOnlyViolation(constraintViolations);
		assertThat(violation.getPropertyPath()).hasToString("lastName");
		assertThat(violation.getMessage()).isEqualTo("must not be blank");
	}

	@Test
	void shouldNotValidateWhenAddressEmpty() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Owner owner = new Owner();
		owner.setFirstName("George");
		owner.setLastName("smith");
		owner.setAddress("");
		owner.setCity("london");
		owner.setTelephone("1234567890");

		Validator validator = createValidator();
		Set<ConstraintViolation<Owner>> constraintViolations = validator.validate(owner);

		ConstraintViolation<Owner> violation = getOnlyViolation(constraintViolations);
		assertThat(violation.getPropertyPath()).hasToString("address");
		assertThat(violation.getMessage()).isEqualTo("must not be blank");
	}

	@Test
	void shouldNotValidateWhenCityEmpty() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Owner owner = new Owner();
		owner.setFirstName("George");
		owner.setLastName("smith");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("");
		owner.setTelephone("1234567890");

		Validator validator = createValidator();
		Set<ConstraintViolation<Owner>> constraintViolations = validator.validate(owner);

		ConstraintViolation<Owner> violation = getOnlyViolation(constraintViolations);
		assertThat(violation.getPropertyPath()).hasToString("city");
		assertThat(violation.getMessage()).isEqualTo("must not be blank");
	}

	@Test
	void shouldNotValidateWhenTelephoneEmpty() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Owner owner = new Owner();
		owner.setFirstName("George");
		owner.setLastName("smith");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("london");
		owner.setTelephone(null);

		Validator validator = createValidator();
		Set<ConstraintViolation<Owner>> constraintViolations = validator.validate(owner);

		ConstraintViolation<Owner> violation = getOnlyViolation(constraintViolations);
		assertThat(violation.getPropertyPath()).hasToString("telephone");
		assertThat(violation.getMessage()).isEqualTo("must not be blank");
	}

	@Test
	void shouldNotValidateWhenTelephoneInvalidFormat() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Owner owner = new Owner();
		owner.setFirstName("George");
		owner.setLastName("smith");
		owner.setAddress("110 W. Liberty St.");
		owner.setCity("london");
		owner.setTelephone("123");

		Validator validator = createValidator();
		Set<ConstraintViolation<Owner>> constraintViolations = validator.validate(owner);

		ConstraintViolation<Owner> violation = getOnlyViolation(constraintViolations);
		assertThat(violation.getPropertyPath()).hasToString("telephone");
		assertThat(violation.getMessage()).isEqualTo("Telephone must be a 10-digit number");
	}

	@Test
	void shouldNotValidateWhenVisitDescriptionEmpty() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Visit visit = new Visit();
		visit.setDescription("");

		Validator validator = createValidator();
		Set<ConstraintViolation<Visit>> constraintViolations = validator.validate(visit);

		ConstraintViolation<Visit> violation = getOnlyViolation(constraintViolations);
		assertThat(violation.getPropertyPath()).hasToString("description");
		assertThat(violation.getMessage()).isEqualTo("must not be blank");
	}

	@Test
	void shouldNotValidateWhenVisitDateNull() {
		LocaleContextHolder.setLocale(Locale.ENGLISH);

		Visit visit = new Visit();
		visit.setDescription("routine checkup");
		visit.setDate(null);

		Validator validator = createValidator();
		Set<ConstraintViolation<Visit>> constraintViolations = validator.validate(visit);

		ConstraintViolation<Visit> violation = getOnlyViolation(constraintViolations);
		assertThat(violation.getPropertyPath()).hasToString("date");
		assertThat(violation.getMessage()).isEqualTo("must not be null");
	}

}
