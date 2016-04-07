package me.fru1t.fanfiction.database.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.Database;

public class Scrape {
	/**
	 * Represents the scrape_raw table in the fanfiction database.
	 */
	public static class ScrapeRaw {
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_SCRAPE_SESSION_ID = "scrape_session_id";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_URL = "url";
		public static final String COLUMN_CONTENT = "content";
		
		/** id INT(11) AI PK */
		public int id;
		
		/** scrape_session_id INT(11) */
		public int scrapeSessionId;
		
		/** date INT(10) */
		public int date;
		
		/** url VARCHAR(255) */
		public String url;
		
		/** content MEDIUMTEXT*/
		public String content;
	}
	
	private static final String INSERT_RAW_SCRAPE = 
			"INSERT INTO raw_scrape (session_id, date, url, content) VALUES (?, ?, ?, ?)";
	
	public static String getRandomRaw() {
		try {
			Connection c = Database.getConnection();
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
	
	public static void insertRaw(@Nullable String sessionId, String url, String content) {
		int currentTime = (int) ((new Date()).getTime() / 1000);
		Connection con = Database.getConnection();
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
}
