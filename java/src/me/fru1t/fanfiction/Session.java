package me.fru1t.fanfiction;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import me.fru1t.util.DatabaseConnectionPool.Statement;

public class Session {
	public enum SessionName {
		CONVERT_ALL_FANDOM_PAGES_16_6_6,
		SCRAPE_ALL_FANDOM_PAGES_16_6_1,
		SCRAPE_CATEGORY_PAGES_16_10_10, 
		SCRAPE_ALL_FANDOM_PAGES_16_10_10, 
		CONVERT_ALL_FANDOM_PAGES_16_10_10, 
		SCRAPE_PROFILE_PAGES_16_10_18, 
		SCRAPE_REVIEW_PAGES_16_11_09,
		CONVERT_PROFILE_PAGES_16_11_10,
	}
	
	private int id;
	private SessionName name;
	
	public Session(SessionName name) throws InterruptedException {
		this.name = name;
		
		// look at the database, check if a session with the specified name 
		// is in the table Session, if yes get that, otherwise create one and return that
		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				CallableStatement stmt = c.prepareCall ("{? = call fn_insfet_session(?)}");
				
				try {
				    stmt.registerOutParameter (1, Types.INTEGER);
				    stmt.setString(2, name.name());    
				    stmt.execute();
				   	id = stmt.getInt (1);
				} finally {
				   	stmt.close();
				}
			}
		});
	}
	
	public int getID() {
		return this.id;
	}
	
	public SessionName getName() {
		return this.name;
	}
}

