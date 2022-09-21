package com.fidelity.integration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fidelity.model.Client;
import com.fidelity.model.ClientRisk;

public class ClientDaoOracleImpl implements ClientDao{
	
	private final Logger logger = LoggerFactory.getLogger(ClientDaoOracleImpl.class);
	
	private Connection conn;
	
	String sqlGet = """
			Select c.client_id as client_id, c.client_name as client_name, c.client_risk as client_risk, p.phone_number as phone_number
			from aa_client c
			Left Join aa_client_phone p
			on c.client_id = p.client_id
			""";
	
	String sqlInsertClient = """
			Insert
			into aa_client (client_id, client_name, client_risk)
			values (?, ?, ?)
			""";
	
	String sqlInsertPhone = """
			Insert
			into aa_client_phone(client_id, phone_number)
			values(?, ?)
			""";
	
	String sqlDeleteClient = """
			Delete
			from aa_client
			where client_id = ?
			""";
	
	String sqlDeletePhone = """
			Delete
			from aa_client_phone
			where client_id = ?
			""";

	@Override
	public List<Client> getClients() {
		List<Client> clients = new ArrayList<Client>();
		Connection conn = getConnection();
		try (PreparedStatement stmt = conn.prepareStatement(sqlGet)) {
			ResultSet rs = stmt.executeQuery();
			clients = getAndHandleResults(stmt);
		} catch (SQLException e) {
			logger.error("Cannot execute getClients: ", e);
			throw new DatabaseException("Cannot execute qgetClients", e);
		}

		return clients;
	}

	private List<Client> getAndHandleResults(PreparedStatement stmt) throws SQLException {
		// TODO Auto-generated method stub
		List<Client> clients = new ArrayList<Client>();
		
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			int clientId = rs.getInt("client_id");
			if (rs.wasNull()) {
				clientId = -1; // invalid
			}
			
			String clientName = rs.getString("client_name");
			if (rs.wasNull()) {
				clientName = null;
			}
			
			String clientRisk = rs.getString("client_risk");
			ClientRisk risk;
			if (rs.wasNull()) {
				risk = null;
			}else {
				risk = ClientRisk.of(clientRisk);
			}
			
			String phone = rs.getString("phone_number");
			if (rs.wasNull()) {
				phone = null;
			}
			Client c = new Client(clientId, clientName, risk, phone);
			clients.add(c);
		}
		
		return clients;
	}

	@Override
	public void insertClient(Client client) {
		
		Connection conn = getConnection();
		DatabaseException ex = null;
		try {
			conn.setAutoCommit(false);
			insertIntoClient(conn, client);
			insertIntoPhone(conn, client);
			conn.commit();
		} catch (SQLException | DatabaseException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			logger.error("Cannot execute insert Client for " + client, e);
			ex = new DatabaseException("Cannot insert client", e);
			throw ex;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				logger.error("Exception setting autoCommit in finally block", e);
				if (ex == null) {
					throw new DatabaseException("Exception setting autoCommit in finally block");
				}
			}
		}
		
	}
	
	private void insertIntoClient(Connection conn, Client client){

		try (PreparedStatement stmt = conn.prepareStatement(this.sqlInsertClient)) {
			stmt.setInt(1, client.getClientId());
			stmt.setString(2, client.getClientName());
			stmt.setString(3, client.getClientRisk().getCode());
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Cannot execute insertiuntoClient for" + client, e);
			throw new DatabaseException("Cannot insert client", e);
		}
		
	}
	private void insertIntoPhone(Connection conn, Client client) {

	
		try (PreparedStatement stmt = conn.prepareStatement(this.sqlInsertPhone)) {
			stmt.setInt(1, client.getClientId());
			stmt.setString(2, client.getWorkPhone());
			
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Cannot execute insertiuntoPohne for" + client, e);
			throw new DatabaseException("Cannot insert client", e);
		}
	}

	@Override
	public void deleteClient(int clientId) {
		// TODO Auto-generated method stub
		Connection conn = getConnection();
		DatabaseException ex = null;
		try {
			conn.setAutoCommit(false);
			deleteClient(conn, clientId);
			deletePhone(conn, clientId);
			conn.commit();
		} catch (SQLException | DatabaseException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			logger.error("Cannot execute deleteClient for " + clientId, e);
			ex = new DatabaseException("Cannot delete client", e);
			throw ex;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				logger.error("Exception setting autoCommit in finally block", e);
				if (ex == null) {
					throw new DatabaseException("Exception setting autoCommit in finally block");
				}
			}
		}
		
	}
	
	private void deletePhone(Connection conn2, int clientId) {
		// TODO Auto-generated method stub
		try (PreparedStatement stmt = conn.prepareStatement(this.sqlDeletePhone)) {
			stmt.setInt(1, clientId);
			
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Cannot execute deletePhone for" + clientId, e);
			throw new DatabaseException("Cannot insert client", e);
		}
	}

	private void deleteClient(Connection conn2, int clientId) {
		// TODO Auto-generated method stub
		try (PreparedStatement stmt = conn.prepareStatement(this.sqlDeletePhone)) {
			stmt.setInt(1, clientId);
			
			stmt.executeUpdate();
		} catch (SQLException e) {
			logger.error("Cannot execute deleteClient for" + clientId, e);
			throw new DatabaseException("Cannot insert client", e);
		}
	}

	private Connection getConnection() {
		if (conn == null) {
			Properties properties = new Properties();
			try {
				properties.load(this.getClass().getClassLoader().getResourceAsStream("db.properties"));
				String dbUrl = properties.getProperty("db.url");
				String user = properties.getProperty("db.username");
				String password = properties.getProperty("db.password");

				conn = DriverManager.getConnection(dbUrl, user, password);
			} catch (IOException e) {
				logger.error("Cannot read db.properties", e);
				throw new DatabaseException("Cannot read db.properties", e);
			} catch (SQLException e) {
				logger.error("Cannot connect", e);
				throw new DatabaseException("Cannot connect", e);
			}
		}
		return conn;
	}
	
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new DatabaseException("Cannot close connection", e);
			} finally {
				conn = null;
			}
		}
	}
}
