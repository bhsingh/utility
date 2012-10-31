package org.biosemantics.common.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtility {
	private static final String USER = "root";
	private static final String PASSWORD = "21biosemantiek!?";
	private static final String URL = "jdbc:mysql://10.218.27.190:3306/Medline_2012";
	private Connection connection;

	public DatabaseUtility() throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection(URL, USER, PASSWORD);
	}

	public DatabaseUtility(String url, String user, String password)
			throws SQLException, ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
		connection = DriverManager.getConnection(url, user, password);
	}

	public Connection getConnection() {
		return connection;
	}

	public Connection openNewConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

}
