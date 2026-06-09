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

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.samples.petclinic.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * Simple JavaBean domain object representing a pet clinic visit.
 *
 * Mapped to the "visits" table in the database. Extends BaseEntity to inherit the ID
 * property.
 *
 * @author Ken Krebs
 * @author Dave Syer
 */
@Entity
@Table(name = "visits")
public class Visit extends BaseEntity {

	/**
	 * Date when the visit occurred. Matches 'yyyy-MM-dd' format.
	 */
	@Column(name = "visit_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate date;

	/**
	 * Textual description detailing the purpose or outcome of the visit.
	 */
	@NotBlank
	private String description;

	/**
	 * Creates a new instance of Visit initialized to tomorrow's date by default.
	 */
	public Visit() {
		// Default visit date is pre-populated as tomorrow's date
		this.date = LocalDate.now().plusDays(1);
	}

	/**
	 * Gets the date of the visit.
	 * @return date of the visit
	 */
	public LocalDate getDate() {
		return this.date;
	}

	/**
	 * Sets the date of the visit.
	 * @param date visit date to set
	 */
	public void setDate(LocalDate date) {
		this.date = date;
	}

	/**
	 * Gets the description of the visit.
	 * @return visit description text
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Sets the description of the visit.
	 * @param description visit description text
	 */
	public void setDescription(String description) {
		this.description = description;
	}

}
