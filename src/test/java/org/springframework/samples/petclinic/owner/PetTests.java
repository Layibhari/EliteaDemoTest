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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link Pet}
 */
class PetTests {

	@Test
	void shouldComputeAgeInWholeYears() {
		Pet pet = new Pet();
		pet.setBirthDate(LocalDate.now().minusYears(3).minusMonths(1));
		assertThat(pet.getAge()).isEqualTo(3);
	}

	@Test
	void shouldNotRoundUpAgeBeforeBirthdayThisYear() {
		Pet pet = new Pet();
		pet.setBirthDate(LocalDate.now().minusYears(3).plusDays(1));
		assertThat(pet.getAge()).isEqualTo(2);
	}

	@Test
	void shouldReturnZeroForPetBornToday() {
		Pet pet = new Pet();
		pet.setBirthDate(LocalDate.now());
		assertThat(pet.getAge()).isZero();
	}

	@Test
	void shouldReturnZeroWhenBirthDateIsNull() {
		Pet pet = new Pet();
		assertThat(pet.getAge()).isZero();
	}

}
