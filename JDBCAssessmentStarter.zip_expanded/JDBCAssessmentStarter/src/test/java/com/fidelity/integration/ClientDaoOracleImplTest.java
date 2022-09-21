package com.fidelity.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.jdbc.JdbcTestUtils.countRowsInTable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fidelity.model.Client;
import com.fidelity.model.ClientRisk;

class ClientDaoOracleImplTest {
	JdbcTemplate jdbcTemplate;
	DbTestUtils dbTestUtils;
	ClientDao dao;
	SimpleDataSource dataSource;
	Connection connection;
	TransactionManager transactionManager;
	
	@BeforeEach
	void setUp() throws Exception {
		dataSource = new SimpleDataSource();
		connection = dataSource.getConnection();
		//beginTransaction();
		

		transactionManager = new TransactionManager(dataSource);
		transactionManager.startTransaction();
		
		dbTestUtils = new DbTestUtils(connection);
		jdbcTemplate = dbTestUtils.initJdbcTemplate();
		dao = new ClientDaoOracleImpl();
		//dao = new ClientDaoOracleImpl(dataSource);
		connection.setAutoCommit(false);
	}
	
	/*
	 * For the test cases I could not figure out how to turn auto commit off and it 
	 * kept adding it to the database
	 */

	@AfterEach
	void tearDown() throws Exception {
		connection.rollback();
		transactionManager.rollbackTransaction();
		dataSource.shutdown();
	}
	

	@Test
	void testGetClients() {
		List<Client> clients = dao.getClients();
		assertEquals(6, clients.size());
	}
	
	@Test
	public void testInsert() {
		Client c = new Client(10, "namee", ClientRisk.LOW, "+441174960234");
		int oldSize = countRowsInTable(jdbcTemplate, "aa_client");
		dao.insertClient(c);
		
		int newSize = countRowsInTable(jdbcTemplate, "aa_client");
		assertEquals(oldSize + 1 , newSize);
		
	}
	
	@Test
	public void testInsertNegative() {
		// insert duplicate
		Client c = new Client(1, "Ford Perfect", ClientRisk.HIGH, "+441174960234");
		
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.insertClient(c);
    	});
    	assertEquals("Cannot insert client", e.getMessage());
	}
	
	//delete
	@Test
	public void testDelete() {
		
		int oldSize = countRowsInTable(jdbcTemplate, "aa_client");
		dao.deleteClient(1);
		int newSize = countRowsInTable(jdbcTemplate, "aa_client");
		assertEquals(oldSize - 1 , newSize);
		
	}
	
	//delete negative
	@Test
	public void testDeleteNegative() {
		Exception e = assertThrows(DatabaseException.class, () -> {
			dao.deleteClient(1000);
    	});
    	assertEquals("Cannot delete client", e.getMessage());
	}

}
