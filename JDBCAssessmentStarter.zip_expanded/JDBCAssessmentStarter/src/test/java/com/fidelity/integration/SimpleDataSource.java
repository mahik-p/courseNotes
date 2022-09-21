package com.fidelity.integration;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.DriverManager;
import java.util.Objects;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * There are several very good open source implementations
 * of the Java DataSource interface and Connection pools to choose from.
 * So there is no need to define our own DataSource from the ground up.
 * However, there is value to peering into the inner workings of a 
 * very simple DataSource (hence the name of this class).
 *  
 *  This simple DataSource manages only one database Connection.
 *  
 * @author ROI Instructor
 *
 */
public class SimpleDataSource implements DataSource {
    private final Logger logger = LoggerFactory.getLogger(SimpleDataSource.class);
    private Connection connection;
    private Connection dbconnection;
    
    public SimpleDataSource() {
    }
    
	/**
	 * The client will call this method to obtain a database Connection.
	 */
    @Override
	public Connection getConnection() {
		try {	
			if (Objects.isNull(connection) || connection.isClosed()) {
				// Open the database connection
				dbconnection = openConnection();
				
				if (Objects.isNull(dbconnection)) {
					connection = null;
				}
				else {
				// Create a dynamic proxy with the database connection as its target
				// The ConnectionDynamicInvocationHandler class is defined at the end of this file
				ConnectionDynamicInvocationHandler proxy = new ConnectionDynamicInvocationHandler(dbconnection, this);
				
				// Assign the dynamic proxy to the connection field
				connection = (Connection) Proxy.newProxyInstance(ConnectionDynamicInvocationHandler.class.getClassLoader(),	
						new Class[] { Connection.class }, proxy);
				}
				
				// Return the dynamic proxy
				return connection;
			}			
		} catch (SQLException | NoSuchMethodException | SecurityException e) {
			String errorMessage = "Error creating database connection";
			logger.error(errorMessage, e);
			throw new DatabaseException(errorMessage, e);
		}
		
		return connection;
	}
	
    /**
     * This method uses the DriverManager to open a connection
     * @return
     */
	private Connection openConnection() {
		Connection connection = null;
		Properties properties = new Properties();
		try {
			properties.load(this.getClass()
								.getClassLoader()
								.getResourceAsStream("db.properties"));
			String dbUrl = properties.getProperty("db.url");
			String user = properties.getProperty("db.username");
			String password = properties.getProperty("db.password");

			connection = DriverManager.getConnection(dbUrl, user, password);
		} catch (IOException e) {
			String errorMessage = "Cannot read db.properties";
			logger.error(errorMessage, e);
			throw new DatabaseException(errorMessage, e);
		} catch (SQLException e) {
			String errorMessage = "Cannot get connection from DriverManager";
			logger.error(errorMessage, e);
			throw new DatabaseException(errorMessage, e);
		}
		
		return connection;
	}

	/**
	 * The shutdown() method should be called to insure that the database
	 * Connection is closed.
	 */	
	public void shutdown()  {
		if (dbconnection != null) {
			try {
				dbconnection.close();
				connection = null;
			} catch (SQLException e) {
				logger.error("Error closing database connection", e);
			}
		}
	}

	public void returnConnection (Connection connection) {
	}
	
	/****** DataSource methods that we are not using *****/
	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}


}

class ConnectionDynamicInvocationHandler implements InvocationHandler {
	Connection connection;
	SimpleDataSource dataSource;
	
	ConnectionDynamicInvocationHandler(Connection target, SimpleDataSource ds) throws NoSuchMethodException, SecurityException {
		connection = target;
		dataSource = ds;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		
		if ("close".equals(method.getName())){
			dataSource.returnConnection(connection);
		}
		else {
			result = method.invoke(connection, args);
		}
		
		
		return result;
	}
	
}
