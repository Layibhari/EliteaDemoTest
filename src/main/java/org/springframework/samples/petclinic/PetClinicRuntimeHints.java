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

package org.springframework.samples.petclinic;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.vet.Vet;

/**
 * RuntimeHintsRegistrar implementation for the PetClinic application.
 *
 * Provides AOT (Ahead-of-Time) compilation hints to the Spring Native compiler. This
 * ensures that resources like SQL scripts and serialized domain entities are preserved
 * and accessible in native executable builds.
 */
public class PetClinicRuntimeHints implements RuntimeHintsRegistrar {

	/**
	 * Registers resource and serialization hints required by GraalVM at runtime.
	 * @param hints runtime hints collector registry
	 * @param classLoader application class loader
	 */
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		// Register DB migration/schema files under 'resources/db/'
		hints.resources().registerPattern("db/*"); // https://github.com/spring-projects/spring-boot/issues/32654
		// Register database configuration and driver schemas
		hints.resources().registerPattern("db/*/*"); // nested db/{h2,mysql,postgres}
		// Register dynamic messages localization property files
		hints.resources().registerPattern("messages/*");
		// Register MySQL default configuration template
		hints.resources().registerPattern("mysql-default-conf");

		// Register entity classes for Java built-in serialization (e.g. for
		// caching/session states)
		hints.serialization().registerType(BaseEntity.class);
		hints.serialization().registerType(Person.class);
		hints.serialization().registerType(Vet.class);
	}

}
