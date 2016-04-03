package me.fru1t.fanfiction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.jdt.annotation.Nullable;

public class Database {
	private static final String SQL_CONNECTION_STRING = 
			"jdbc:mysql://localhost/fanfiction?user=fanfiction&password=mypwISsoSecure";
	
	private static final String INSERT_RAW_SCRAPE = 
			"INSERT INTO raw_scrape (session_id, date, url, content) VALUES (?, ?, ?, ?)";
	
	private static Connection connection;
	
	public static void insertRawScrape(@Nullable String sessionId, String url, String content) {
		int currentTime = (int) ((new Date()).getTime() / 1000);
		Connection con = getConnection();
		if (con == null) {
			Boot.log("Ignored raw scrape insert as the database couldn't be reached");
			return;
		}
		if (sessionId == null) {
			Boot.log("Ignored raw scrape insert as the content was null");
			return;
		}
		
		try {
			PreparedStatement stmt = con.prepareStatement(INSERT_RAW_SCRAPE);
			stmt.setString(1, sessionId);
			stmt.setInt(2, currentTime);
			stmt.setString(3, url);
			stmt.setString(4, content);
			stmt.execute();
		} catch (SQLException e) {
			Boot.log(e, null);
		}
	}
	
	public static String getRandomBookSearchResult() {
		try {
			Connection c = getConnection();
			if (c == null) {
				Boot.log("No connection");
				return "";
			}
			PreparedStatement stmt = c.prepareStatement("SELECT content FROM raw_scrape WHERE raw_scrape_id = 5");
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				return result.getString(1);
			}
			
		} catch (SQLException e) {
			Boot.log(e, null);
		}
		

		Boot.log("No results");
		return "";
	}
	
	@Nullable
	private static Connection getConnection() {
		if (connection == null) {
			try { 
				connection = DriverManager.getConnection(SQL_CONNECTION_STRING);
			} catch (SQLException e) {
				Boot.log(e, null);
				connection = null;
			}
		}
		return connection;
	}
}
