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

package org.springframework.ldap.repository.support;

import java.util.List;

import javax.naming.Name;

import org.springframework.LdapDataEntry;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.odm.core.ObjectDirectoryMapper;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.repository.SpringLdapRepository;
import org.springframework.util.Assert;

/**
 * Simple implementation of {@link SpringLdapRepository} using {@link LdapClient} and
 * {@link ObjectDirectoryMapper} for LDAP operations.
 *
 * @param <T> the domain type the repository manages
 * @author Emanuel Trandafir
 * @since 3.3
 */
public class SimpleSpringLdapRepository<T> implements SpringLdapRepository<T> {

	private final LdapClient ldapClient;

	private final ObjectDirectoryMapper objectDirectoryMapper;

	private final Class<T> entityClass;

	/**
	 * Creates a new {@link SimpleSpringLdapRepository} for the given entity class.
	 * @param ldapClient must not be {@literal null}
	 * @param objectDirectoryMapper must not be {@literal null}
	 * @param entityClass must not be {@literal null}
	 */
	public SimpleSpringLdapRepository(LdapClient ldapClient, ObjectDirectoryMapper objectDirectoryMapper,
			Class<T> entityClass) {
		Assert.notNull(ldapClient, "LdapClient must not be null");
		Assert.notNull(objectDirectoryMapper, "ObjectDirectoryMapper must not be null");
		Assert.notNull(entityClass, "Entity class must not be null");

		this.ldapClient = ldapClient;
		this.objectDirectoryMapper = objectDirectoryMapper;
		this.entityClass = entityClass;
	}

	@Override
	public List<T> findAll() {
		LdapQuery query = LdapQueryBuilder.query().filter(this.objectDirectoryMapper.filterFor(this.entityClass, null));

		return this.ldapClient.search().query(query).toEntryStream().map(this::mapFromLdapDataEntry).toList();
	}

	@Override
	public T save(T entity) {
		if (entity == null) {
			throw new IllegalArgumentException("Entity must not be null");
		}
		Name calculatedId = this.objectDirectoryMapper.getCalculatedId(entity);
		if (calculatedId == null) {
			throw new IllegalArgumentException("Entity must have a valid DN (calculatedId)");
		}

		DirContextAdapter context = new DirContextAdapter(calculatedId);
		this.objectDirectoryMapper.mapToLdapDataEntry(entity, context);

		this.ldapClient.bind(calculatedId).attributes(context.getAttributes()).replaceExisting(true).execute();

		return entity;
	}

	private T mapFromLdapDataEntry(LdapDataEntry entry) {
		return this.objectDirectoryMapper.mapFromLdapDataEntry(entry, this.entityClass);
	}

}
