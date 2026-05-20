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
package org.springframework.samples.petclinic.vet;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.SerializationUtils;

/**
 * @author Dave Syer
 */
class VetTests {

	private Vet vet;

	private Specialty radiology;

	private Specialty surgery;

	private Specialty dentistry;

	@BeforeEach
	void setUp() {
		vet = new Vet();
		vet.setFirstName("James");
		vet.setLastName("Carter");
		vet.setId(1);

		radiology = new Specialty();
		radiology.setId(1);
		radiology.setName("radiology");

		surgery = new Specialty();
		surgery.setId(2);
		surgery.setName("surgery");

		dentistry = new Specialty();
		dentistry.setId(3);
		dentistry.setName("dentistry");
	}

	@Test
	void serialization() {
		Vet other = new Vet();
		other.setFirstName("Zaphod");
		other.setLastName("Beeblebrox");
		other.setId(123);
		@SuppressWarnings("deprecation")
		Vet deserialized = (Vet) SerializationUtils.deserialize(SerializationUtils.serialize(other));
		assertThat(deserialized.getFirstName()).isEqualTo(other.getFirstName());
		assertThat(deserialized.getLastName()).isEqualTo(other.getLastName());
		assertThat(deserialized.getId()).isEqualTo(other.getId());
	}

	@Test
	void newVetHasNoSpecialties() {
		assertThat(vet.getNrOfSpecialties()).isZero();
		assertThat(vet.getSpecialties()).isEmpty();
	}

	@Test
	void addSpecialtyIncrementsCount() {
		vet.addSpecialty(radiology);
		assertThat(vet.getNrOfSpecialties()).isEqualTo(1);
	}

	@Test
	void addMultipleSpecialtiesIncrementsCount() {
		vet.addSpecialty(radiology);
		vet.addSpecialty(surgery);
		assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
	}

	@Test
	void getSpecialtiesReturnsSortedAlphabetically() {
		vet.addSpecialty(surgery);
		vet.addSpecialty(dentistry);
		vet.addSpecialty(radiology);
		assertThat(vet.getSpecialties()).extracting(Specialty::getName)
			.containsExactly("dentistry", "radiology", "surgery");
	}

	@Test
	void addDuplicateSpecialtyIsIgnored() {
		vet.addSpecialty(radiology);
		vet.addSpecialty(radiology);
		assertThat(vet.getNrOfSpecialties()).isEqualTo(1);
	}

}
