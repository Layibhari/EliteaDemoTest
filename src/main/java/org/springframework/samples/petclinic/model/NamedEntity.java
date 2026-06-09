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

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;

/**
 * Simple JavaBean domain object adds a name property to <code>BaseEntity</code>. Used as
 * a base class for objects needing these properties.
 * 
 * Subclasses include Specialty and PetType, which both rely on name-based identification.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Wick Dynex
 */
@MappedSuperclass
public class NamedEntity extends BaseEntity {

	/**
	 * Name value for the entity. Can neither be null nor empty.
	 */
	@Column
	@NotBlank
	private String name;

	/**
	 * Gets the name of the entity.
	 * @return the name string
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of the entity.
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * String representation of the NamedEntity, returning its name.
	 * @return the entity name, or "<null>" if the name is not set
	 */
	@Override
	public String toString() {
		String name = this.getName();
		// Return the name if it is not null, otherwise fallback to placeholder <null>
		return name != null ? name : "<null>";
	}

}
