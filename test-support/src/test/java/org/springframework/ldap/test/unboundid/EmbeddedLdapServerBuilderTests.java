/*
 * Copyright 2005-2025 the original author or authors.
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

package org.springframework.ldap.test.unboundid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.LdapQueryBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedLdapServerBuilderTests {

	private static String tempLogFile;

	@BeforeClass
	public static void before() throws IOException {
		tempLogFile = Files.createTempFile("ldap-log-", ".txt").toAbsolutePath().toString();
	}

	@Test
	public void shouldBuildButNotStartTheServer() {
		int port = SocketUtils.getFreePort();

		EmbeddedLdapServer.builder().withPort(port).build();

		assertThat(SocketUtils.isPortOpen(port)).isFalse();
	}

	@Test
	public void shouldBuildTheServerWithCustomPort() {
		int port = SocketUtils.getFreePort();

		EmbeddedLdapServerBuilder serverBuilder = EmbeddedLdapServer.builder()
			.withPartitionName("test")
			.withPartitionSuffix("dc=test,dc=se")
			.withPort(port);

		try (EmbeddedLdapServer server = serverBuilder.build()) {
			server.start();
			assertThat(SocketUtils.isPortOpen(port)).isTrue();
		}
		assertThat(SocketUtils.isPortOpen(port)).isFalse();
	}

	@Test
	public void shouldBuildLdapServerAndApplyCustomConfiguration() {
		int port = SocketUtils.getFreePort();

		EmbeddedLdapServerBuilder serverBuilder = EmbeddedLdapServer.builder()
			.withPort(port)
			.withPartitionName("jayway")
			.withPartitionSuffix("dc=jayway,dc=se")
			.withConfigurationCustomizer((config) -> config.setCodeLogDetails(tempLogFile, true));

		try (EmbeddedLdapServer server = serverBuilder.build()) {
			server.start();

			ldapTemplate("dc=jayway,dc=se", port).search(LdapQueryBuilder.query().where("objectclass").is("person"),
					new AttributesMapper<>() {
						public String mapFromAttributes(Attributes attrs) throws NamingException {
							return (String) attrs.get("cn").get();
						}
					});
		}

		assertThat(Path.of(tempLogFile))
			.as("Applying the custom configuration should create a log file and populate it with the request")
			.isNotEmptyFile();
	}

	private static LdapTemplate ldapTemplate(String base, int port) {
		LdapContextSource ctx = new LdapContextSource();
		ctx.setBase(base);
		ctx.setUrl("ldap://127.0.0.1:" + port);
		ctx.setUserDn("uid=admin,ou=system");
		ctx.setPassword("secret");
		ctx.afterPropertiesSet();
		return new LdapTemplate(ctx);
	}

}
