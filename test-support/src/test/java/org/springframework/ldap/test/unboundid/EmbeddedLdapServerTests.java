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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class EmbeddedLdapServerTests {

	@Test
	public void shouldStartAndCloseServer() throws Exception {
		int port = SocketUtils.getFreePort();
		assertThat(SocketUtils.isPortOpen(port)).isFalse();

		EmbeddedLdapServer server = EmbeddedLdapServer.newEmbeddedServer("jayway", "dc=jayway,dc=se", port);
		assertThat(SocketUtils.isPortOpen(port)).isTrue();

		server.close();
		assertThat(SocketUtils.isPortOpen(port)).isFalse();
	}

	@Test
	public void shouldStartAndAutoCloseServer() throws Exception {
		int port = SocketUtils.getFreePort();
		assertThat(SocketUtils.isPortOpen(port)).isFalse();

		try (EmbeddedLdapServer ignored = EmbeddedLdapServer.newEmbeddedServer("jayway", "dc=jayway,dc=se", port)) {
			assertThat(SocketUtils.isPortOpen(port)).isTrue();
		}
		assertThat(SocketUtils.isPortOpen(port)).isFalse();
	}

	@Test
	public void shouldStartAndCloseServerViaLdapTestUtils() throws Exception {
		int port = SocketUtils.getFreePort();
		assertThat(SocketUtils.isPortOpen(port)).isFalse();

		LdapTestUtils.startEmbeddedServer(port, "dc=jayway,dc=se", "jayway");
		assertThat(SocketUtils.isPortOpen(port)).isTrue();

		LdapTestUtils.shutdownEmbeddedServer();
		assertThat(SocketUtils.isPortOpen(port)).isFalse();
	}

	@Test
	public void startWhenNewEmbeddedServerThenException() throws Exception {
		int port = SocketUtils.getFreePort();
		EmbeddedLdapServer server = EmbeddedLdapServer.newEmbeddedServer("jayway", "dc=jayway,dc=se", port);
		assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(server::start);
	}

	@Test
	public void startWhenUnstartedThenWorks() throws Exception {
		int port = SocketUtils.getFreePort();
		InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=jayway,dc=se");
		config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", port));
		InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
		try (EmbeddedLdapServer server = new EmbeddedLdapServer(ds)) {
			server.start();
			assertThat(SocketUtils.isPortOpen(port)).isTrue();
		}
	}

	@Test
	public void startWhenAlreadyStartedThenFails() throws Exception {
		int port = SocketUtils.getFreePort();
		InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=jayway,dc=se");
		config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", port));
		InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
		try (EmbeddedLdapServer server = new EmbeddedLdapServer(ds)) {
			server.start();
			assertThat(SocketUtils.isPortOpen(port)).isTrue();
			assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(server::start);
		}
	}

}
