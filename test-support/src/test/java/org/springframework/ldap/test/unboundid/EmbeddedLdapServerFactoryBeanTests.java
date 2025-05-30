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

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.junit.After;
import org.junit.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddedLdapServerFactoryBeanTests {

	ClassPathXmlApplicationContext ctx;

	@After
	public void setup() {
		if (this.ctx != null) {
			this.ctx.close();
		}
	}

	@Test
	public void testServerStartup() throws Exception {
		this.ctx = new ClassPathXmlApplicationContext("/applicationContext-ldifPopulator.xml");
		LdapTemplate ldapTemplate = this.ctx.getBean(LdapTemplate.class);
		assertThat(ldapTemplate).isNotNull();

		List<String> list = ldapTemplate.search(LdapQueryBuilder.query().where("objectclass").is("person"),
				new AttributesMapper<>() {
					public String mapFromAttributes(Attributes attrs) throws NamingException {
						return (String) attrs.get("cn").get();
					}
				});
		assertThat(list.size()).isEqualTo(5);
	}

}
