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

package org.springframework.ldap.repository.integration;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import javax.naming.Name;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ldap.core.LdapClient;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.core.impl.DefaultObjectDirectoryMapper;
import org.springframework.ldap.repository.SpringLdapRepository;
import org.springframework.ldap.repository.support.SimpleSpringLdapRepository;
import org.springframework.ldap.test.unboundid.EmbeddedLdapServer;

/**
 * Integration tests for {@link SpringLdapRepository} using an embedded LDAP server from
 * the test-support module.
 *
 * @author Emanuel Trandafir
 */
class SpringLdapRepositoryTests {

	private static final String LDAP_BASE = "dc=example,dc=com";

	private static final int LDAP_PORT = getFreePort();

	private SpringLdapRepository<TestPerson> repository = new SimpleSpringLdapRepository<>(ldapClient(),
			new DefaultObjectDirectoryMapper(), TestPerson.class);

	@AutoClose
	private static EmbeddedLdapServer ldapServer = EmbeddedLdapServer.withPartitionSuffix(LDAP_BASE)
		.port(LDAP_PORT)
		.build();

	@BeforeAll
	static void setup() {
		ldapServer.start();
	}

	@Test
	void findAllShouldReturnEmptyListWhenNoEntries() {
		List<TestPerson> result = this.repository.findAll();

		BDDAssertions.then(result).isNotNull().isEmpty();
	}

	@Test
	void saveShouldCreatePersonInLdap() {
		TestPerson person = new TestPerson("John Doe", "DOE");

		this.repository.save(person);

		BDDAssertions.then(this.repository.findAll()).extracting(TestPerson::getCommonName).containsExactly("John Doe");
	}

	@Entry(objectClasses = { "inetOrgPerson", "organizationalPerson", "person", "top" })
	public static class TestPerson {

		@Id
		private Name dn;

		@Attribute(name = "cn")
		@DnAttribute(value = "cn", index = 0)
		private String commonName;

		@Attribute(name = "sn")
		private String surname;

		public TestPerson() {
		}

		public TestPerson(String commonName, String surname) {
			this.commonName = commonName;
			this.surname = surname;
		}

		public Name getDn() {
			return this.dn;
		}

		public void setDn(Name dn) {
			this.dn = dn;
		}

		public String getCommonName() {
			return this.commonName;
		}

		public void setCommonName(String commonName) {
			this.commonName = commonName;
		}

		public String getSurname() {
			return this.surname;
		}

		public void setSurname(String surname) {
			this.surname = surname;
		}

		@Override
		public String toString() {
			return "{ dn=%s, commonName=%s, surname=%s}".formatted(this.dn, this.commonName, this.surname);
		}

	}

	private static LdapClient ldapClient() {
		LdapContextSource ctx = new LdapContextSource();
		ctx.setUrl(String.format("ldap://localhost:%s", LDAP_PORT));
		ctx.setBase(LDAP_BASE);
		ctx.afterPropertiesSet();
		return LdapClient.create(new LdapTemplate(ctx));
	}

	private static int getFreePort() {
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			return serverSocket.getLocalPort();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
