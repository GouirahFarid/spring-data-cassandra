/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.core;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.data.cassandra.core.query.Criteria.*;
import static org.springframework.data.cassandra.core.query.Query.*;

import lombok.Data;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.cql.CqlIdentifier;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.test.util.AbstractKeyspaceCreatingIntegrationTest;

/**
 * Integration tests for {@link ExecutableDeleteOperationSupport}.
 *
 * @author Mark Paluch
 */
public class ExecutableDeleteOperationSupportTests extends AbstractKeyspaceCreatingIntegrationTest {

	CassandraAdminTemplate template;

	Person han;
	Person luke;

	@Before
	public void setUp() {

		template = new CassandraAdminTemplate(session, new MappingCassandraConverter());
		template.dropTable(true, CqlIdentifier.of("person"));
		template.createTable(true, CqlIdentifier.of("person"), ExecutableInsertOperationSupportTests.Person.class,
				Collections.emptyMap());

		han = new Person();
		han.firstname = "han";
		han.id = "id-1";

		luke = new Person();
		luke.firstname = "luke";
		luke.id = "id-2";

		template.insert(han);
		template.insert(luke);
	}

	@Test // DATACASS-485
	public void removeAllMatching() {

		WriteResult writeResult = template.delete(Person.class).matching(query(where("id").is(han.id))).all();

		assertThat(writeResult.wasApplied()).isTrue();
	}

	@Test // DATACASS-485
	public void removeAllMatchingWithAlternateDomainTypeAndCollection() {

		WriteResult writeResult = template.delete(Jedi.class).inTable("person")
				.matching(query(where("id").in(han.id, luke.id)))
				.all();

		assertThat(writeResult.wasApplied()).isTrue();
		assertThat(template.select(Query.empty(), Person.class)).isEmpty();
	}

	@Data
	@Table
	static class Person {
		@Id String id;
		@Indexed String firstname;
	}

	@Data
	static class Jedi {

		@Column("firstname") String name;
	}
}