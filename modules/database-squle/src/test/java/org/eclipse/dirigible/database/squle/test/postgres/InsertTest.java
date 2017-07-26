package org.eclipse.dirigible.database.squle.test.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.dirigible.database.squle.Squle;
import org.eclipse.dirigible.database.squle.dialects.postgres.PostgresSquleDialect;
import org.junit.Test;

public class InsertTest {
	
	@Test
	public void insertSimple() {
		String sql = Squle.getNative(new PostgresSquleDialect())
			.insert()
			.into("CUSTOMERS")
			.column("FIRST_NAME")
			.column("LAST_NAME")
			.toString();
		
		assertNotNull(sql);
		assertEquals("INSERT INTO CUSTOMERS (FIRST_NAME, LAST_NAME) VALUES (?, ?)", sql);
	}
	
	@Test
	public void insertValues() {
		String sql = Squle.getNative(new PostgresSquleDialect())
			.insert()
			.into("CUSTOMERS")
			.column("FIRST_NAME")
			.column("LAST_NAME")
			.value("?")
			.value("'Smith'")
			.toString();

		assertNotNull(sql);
		assertEquals("INSERT INTO CUSTOMERS (FIRST_NAME, LAST_NAME) VALUES (?, 'Smith')", sql);
	}
	
	@Test
	public void insertSelect() {
		String sql = Squle.getNative(new PostgresSquleDialect())
			.insert()
			.into("CUSTOMERS")
			.column("FIRST_NAME")
			.column("LAST_NAME")
			.select(Squle.getNative(new PostgresSquleDialect()).select().column("*").from("SUPPLIERS").toString())
			.toString();

		assertNotNull(sql);
		assertEquals("INSERT INTO CUSTOMERS (FIRST_NAME, LAST_NAME) SELECT * FROM SUPPLIERS", sql);
	}

}
