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
import jakarta.validation.constraints.Size;

/**
 * Simple JavaBean domain object representing a person.
 *
 * Subclassed by components that require general contact/personal details (e.g. Vet,
 * Owner). Uses Hibernate/JPA mapping attributes to bind properties to db columns.
 *
 * @author Ken Krebs
 */
@MappedSuperclass
public class Person extends BaseEntity {

	/**
	 * First name of the person. Limited to 30 characters and cannot be blank.
	 */
	@Column(length = 30)
	@Size(max = 30)
	@NotBlank
	private String firstName;

	/**
	 * Last name of the person. Limited to 30 characters and cannot be blank.
	 */
	@Column(length = 30)
	@Size(max = 30)
	@NotBlank
	private String lastName;

	/**
	 * Gets the first name of the person.
	 * @return first name string
	 */
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 * Sets the first name of the person.
	 * @param firstName first name to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Gets the last name of the person.
	 * @return last name string
	 */
	public String getLastName() {
		return this.lastName;
	}

	/**
	 * Sets the last name of the person.
	 * @param lastName last name to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

}
