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

import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Repository interface for <code>Vet</code> domain objects.
 *
 * Extends Spring Data {@link Repository} interface. Implements read-only transactions and
 * caching layers to optimize retrieval performance of vets data.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface VetRepository extends Repository<Vet, Integer> {

	/**
	 * Retrieve all <code>Vet</code>s from the data store.
	 *
	 * Runs within a read-only transaction and caches findings under the "vets" cache
	 * name.
	 * @return a <code>Collection</code> of <code>Vet</code>s
	 * @throws DataAccessException if database retrieval fails
	 */
	@Transactional(readOnly = true)
	@Cacheable("vets")
	Collection<Vet> findAll() throws DataAccessException;

	/**
	 * Retrieve all <code>Vet</code>s from the data store with pagination support.
	 *
	 * Runs within a read-only transaction and caches findings under the "vets" cache
	 * name.
	 * @param pageable requested page parameters (page number, page size, sort criteria)
	 * @return a <code>Page</code> of <code>Vet</code>s
	 * @throws DataAccessException if database retrieval fails
	 */
	@Transactional(readOnly = true)
	@Cacheable("vets")
	Page<Vet> findAll(Pageable pageable) throws DataAccessException;

}
