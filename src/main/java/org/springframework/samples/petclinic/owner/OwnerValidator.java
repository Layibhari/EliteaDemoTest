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
 * <code>Validator</code> for <code>Owner</code> forms.
 * <p>
 * We're not using Bean Validation annotations here because it is easier to define such
 * validation rule in Java.
 * </p>
 *
 * @author Deep Swarup
 */
public class OwnerValidator implements Validator {

	private static final String REQUIRED = "required";

	@Override
	public void validate(Object obj, Errors errors) {
		Owner owner = (Owner) obj;
		validNameValue(owner.getFirstName(), "firstName", errors);
		validNameValue(owner.getLastName(), "lastName", errors);
	}

	private void validNameValue (String name, String field, Errors errors) {
		if (!StringUtils.hasText(name)) {
			errors.rejectValue(field, REQUIRED, REQUIRED);
		} else if (!StringUtils.hasLength(name)) {
			errors.rejectValue(field, "length", "length must be greater than 0");
		} else if (!name.matches(".*[a-zA-Z].*")) {
			errors.rejectValue(field, "pattern", "must contain at least one letter");
		} else if (name.length() > 30) {
			errors.rejectValue(field, "length", "length must be less than 30");
		}
	}

	/**
	 * This Validator validates *just* Pet instances
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return Owner.class.isAssignableFrom(clazz);
	}

}
