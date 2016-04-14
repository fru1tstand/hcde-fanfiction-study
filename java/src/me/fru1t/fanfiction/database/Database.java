package me.fru1t.fanfiction.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import me.fru1t.fanfiction.Boot;

public class Database {
	private static final String SQL_CONNECTION_STRING = 
			"jdbc:mysql://localhost/fanfiction?user=fanfiction&password=mypwISsoSecure";

	private static Connection connection;

	public static synchronized Connection getConnection() {
		if (connection == null) {
			try { 
				connection = DriverManager.getConnection(SQL_CONNECTION_STRING);
			} catch (SQLException e) {
				Boot.getLogger().log(e);
				throw new RuntimeException("Connection to the database couldn't be established", e);
			}
		}
		return connection;
	}
}
