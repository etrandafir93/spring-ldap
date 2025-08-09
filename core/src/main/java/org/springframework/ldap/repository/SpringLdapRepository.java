/*
 * Copyright 2006-present the original author or authors.
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

package org.springframework.ldap.repository;

import java.util.List;

/**
 * Spring Data-like repository interface for LDAP operations using LdapClient.
 *
 * @param <T> the domain type the repository manages
 * @author Emanuel Trandafir
 * @since 3.3
 */
public interface SpringLdapRepository<T> {

	/**
	 * Returns all instances of the type.
	 * @return all entities
	 */
	List<T> findAll();

	/**
	 * Saves a given entity. Use the returned instance for further operations as the save
	 * operation might have changed the entity instance completely.
	 * @param entity must not be {@literal null}
	 * @return the saved entity; will never be {@literal null}
	 * @throws IllegalArgumentException if {@literal entity} is {@literal null}
	 */
	T save(T entity);

}
