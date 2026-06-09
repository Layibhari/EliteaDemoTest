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

import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

/**
 * Instructs Spring MVC on how to parse and print elements of type 'PetType'. Starting
 * from Spring 3.0, Formatters have come as an improvement in comparison to legacy
 * PropertyEditors. See the following links for more details: - The Spring ref doc:
 * https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#format
 *
 * Auto-detected as a Spring @Component and automatically registered in Spring MVC
 * formatter registry.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Michael Isvy
 */
@Component
public class PetTypeFormatter implements Formatter<PetType> {

	/**
	 * Repository providing database access to look up valid pet types.
	 */
	private final PetTypeRepository types;

	/**
	 * Constructor injecting the repository dependency.
	 * @param types repository for PetType entity
	 */
	public PetTypeFormatter(PetTypeRepository types) {
		this.types = types;
	}

	/**
	 * Prints a PetType object back as a string for display in UI forms.
	 * @param petType target PetType to represent
	 * @param locale requested local contextual information
	 * @return the name of the pet type, or "<null>" if empty
	 */
	@Override
	public String print(PetType petType, Locale locale) {
		String name = petType.getName();
		// Return name if present, otherwise fallback to empty description marker
		return name != null ? name : "<null>";
	}

	/**
	 * Parses a string input from UI form (representing pet type name) into a PetType
	 * entity.
	 * @param text text submitted from input selection
	 * @param locale requested local contextual information
	 * @return matching PetType entity from DB
	 * @throws ParseException if no matching pet type with name exists
	 */
	@Override
	public PetType parse(String text, Locale locale) throws ParseException {
		// Fetch all valid pet types from repository
		Collection<PetType> findPetTypes = this.types.findPetTypes();
		// Iterate and compare names to find the matching entity
		for (PetType type : findPetTypes) {
			if (Objects.equals(type.getName(), text)) {
				return type;
			}
		}
		// Throw exception if the client provided an invalid type name
		throw new ParseException("type not found: " + text, 0);
	}

}
