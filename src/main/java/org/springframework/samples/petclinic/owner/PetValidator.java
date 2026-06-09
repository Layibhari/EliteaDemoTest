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

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * <code>Validator</code> for <code>Pet</code> forms.
 * <p>
 * Custom Spring validator designed specifically for {@link Pet} entity validation rules.
 * Bean Validation annotations are omitted here to handle business logic constraints
 * programmatically.
 * </p>
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
public class PetValidator implements Validator {

	/**
	 * Error code key mapping to error message resource properties (typically resolves to
	 * 'required').
	 */
	private static final String REQUIRED = "required";

	/**
	 * Validates a Pet target object for required fields and logical constraints.
	 * @param obj target object to validate (must be an instance of Pet class)
	 * @param errors binding registry capturing validation failure messages
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		Pet pet = (Pet) obj;
		String name = pet.getName();

		// Validate that the pet's name is not null, empty, or whitespace-only
		if (!StringUtils.hasText(name)) {
			errors.rejectValue("name", REQUIRED, REQUIRED);
		}

		// Validate that a newly registered pet must have an assigned pet type (species)
		if (pet.isNew() && pet.getType() == null) {
			errors.rejectValue("type", REQUIRED, REQUIRED);
		}

		// Validate that the pet has a registered birthdate value
		if (pet.getBirthDate() == null) {
			errors.rejectValue("birthDate", REQUIRED, REQUIRED);
		}
	}

	/**
	 * Verifies if the validator supports the target class model.
	 * @param clazz class context to test
	 * @return true if the class is assignable from Pet, false otherwise
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		// Validates *just* Pet instances or subclasses of Pet
		return Pet.class.isAssignableFrom(clazz);
	}

}
