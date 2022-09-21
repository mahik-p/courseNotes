package com.fidelity.integration;

import java.util.List;

import javax.sql.DataSource;

import com.fidelity.model.Client;

public interface ClientDao {

	
	// you may not change any of these methods
	public List<Client> getClients();
	public void insertClient(Client client);
	public void deleteClient(int clientId);
}
