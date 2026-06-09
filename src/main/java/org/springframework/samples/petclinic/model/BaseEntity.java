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

import java.io.Serializable;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Simple JavaBean domain object with an id property. Used as a base class for objects
 * needing this property.
 *
 * Configured as a JPA MappedSuperclass, meaning its fields are mapped to the tables of
 * subclasses.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 */
@MappedSuperclass
public class BaseEntity implements Serializable {

	/**
	 * Unique identifier for the entity. Annotated with @Id to declare it as primary key,
	 * and set to auto-increment identity strategy.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * Gets the unique ID of the entity.
	 * @return the entity id, or null if it's not persisted yet
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the unique ID of the entity.
	 * @param id the entity id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Checks if the entity is new and has not been persisted in the database yet.
	 * @return true if the ID is null (not saved yet), false otherwise
	 */
	public boolean isNew() {
		// If the ID is null, this instance represents a new record that has not yet been
		// saved to DB
		return this.id == null;
	}

}
