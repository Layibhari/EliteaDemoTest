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
 * Simple JavaBean domain object representing a vaccine record.
 *
 * @author Spring PetClinic
 */
@Entity
@Table(name = "vaccines")
public class Vaccine extends BaseEntity {

	@Column(name = "vaccine_name")
	@NotBlank
	private String name;

	@Column(name = "vaccination_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate vaccinationDate;

	@Column(name = "next_reminder_date")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate nextReminderDate;

	/**
	 * Creates a new instance of Vaccine for the current date
	 */
	public Vaccine() {
		this.vaccinationDate = LocalDate.now();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDate getVaccinationDate() {
		return this.vaccinationDate;
	}

	public void setVaccinationDate(LocalDate vaccinationDate) {
		this.vaccinationDate = vaccinationDate;
	}

	public LocalDate getNextReminderDate() {
		return this.nextReminderDate;
	}

	public void setNextReminderDate(LocalDate nextReminderDate) {
		this.nextReminderDate = nextReminderDate;
	}

}
