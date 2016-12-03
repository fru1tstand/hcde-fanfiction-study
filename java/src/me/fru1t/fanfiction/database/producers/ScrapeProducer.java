package me.fru1t.fanfiction.database.producers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Provides a thread-safe interface for fetching raw scrapes from the fanfiction database. Due
 * to memory constraints and the size of the dataset, we can't simply fetch all scrapes,
 * so this class provides a means of queuing the raw scrapes from the database in a thread-safe
 * manner to allow multi-threaded processing.
 *
 * This provider will always return scrapes from oldest to newest (lowest id to highest).
 */
public class ScrapeProducer extends DatabaseProducer<ScrapeProducer.Scrape, Integer> {
	/**
	 * Represents the scrape_raw table in the fanfiction database.
	 */
	public static class Scrape extends DatabaseProducer.Row<Integer> {
		public static final String COLUMN_SESSION_ID = "sessionId";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_URL = "url";
		public static final String COLUMN_CONTENT = "content";

		/** scrape_session_id INT(11) */
		public int sessionId;

		/** date INT(10) */
		public int date;

		/** url VARCHAR(2000) */
		public String url;

		/** content MEDIUMTEXT*/
		public String content;
	}

	private static final int BUFFER_SIZE = 1000;
	private static final String ID_NAME = "`%s`.`id`";
	private static final String QUERY_BASE =
			"SELECT"
			+ " `id` AS `" + Scrape.COLUMN_ID
			+ "`, `session_id` AS `" + Scrape.COLUMN_SESSION_ID
			+ "`, `date` AS `" + Scrape.COLUMN_DATE
			+ "`, `url` AS `" + Scrape.COLUMN_URL
			+ "`, `content` AS `" + Scrape.COLUMN_CONTENT
			+ "` FROM `%s` "
			+ "WHERE 1 = 1 ";
	
	private static final String SESSION_QUERY_BASE_FMT =
			"SELECT `id` FROM `session` WHERE `name` IN ('%s')";
	private static final String SESSION_RESTRICT_FMT = "AND (%s) ";
	private static final String SESSION_RESTRICT_PART_FMT = "`session_id` = %d";

	@Nullable
	private static final String[] DEFAULT_SCRAPE_SESSIONS = {};

	private String scraping_session_names;
	private String[] sessionNameStrings;
	private String sessionIdSql;
	
	/**
	 * Creates a new provider that only returns scrapes between the given range and belong to
	 * the given session names.
	 *
	 * Note: It's possible to define an illegal bound range without causing an exception. This
	 * will, however, return with 0 results, and be effectively useless.
	 * But, I mean, who am I to judge.
	 *
	 * @param lowerIdBound The lowest ID number to return (exclusive). Set to a negative number to
	 * disable lower bound.
	 * @param upperIdBound The highest ID number to return (exclusive). Set to a negative number to
	 * disable upper bound.
	 * @param sessionNames The sessions with which scrapes should be included from. Set to an
	 * empty array to include all.
	 * @throws InterruptedException
	 */
	public ScrapeProducer(@Nullable String... sessNames) throws InterruptedException {
		super(String.format(ID_NAME, Boot.getScrapeTablename()), Scrape.class, Boot.getDatabaseConnectionPool(),
				BUFFER_SIZE, Boot.getLogger());
		
		// Sanitize session names
		this.sessionNameStrings = new String[sessNames.length];
		this.sessionIdSql = "";
		for (int i = 0; i < sessNames.length; i++) {
			String s = sessNames[i];
			if (s != null) {
				this.sessionNameStrings[i] = s.replaceAll("'", "\\'");
			}
		}

		this.scraping_session_names = String.join(",", sessionNameStrings);
		
		// Fetch sessionids
		String[] sessionParts = new String[sessNames.length];
		try {
			Connection c = Boot.getDatabaseConnectionPool().getConnection();
			String getSessionIdsQuery =
					String.format(SESSION_QUERY_BASE_FMT, String.join("','", this.sessionNameStrings));
			PreparedStatement stmt = c.prepareStatement(getSessionIdsQuery);
			ResultSet result = stmt.executeQuery();
			int i = 0;
			while (result.next()) {
				sessionParts[i] = String.format(SESSION_RESTRICT_PART_FMT, result.getInt(1));
				i++;
			}
		} catch (SQLException e) {
			Boot.getLogger().log(e);
			throw new RuntimeException(e);
		}
		this.sessionIdSql = String.format(SESSION_RESTRICT_FMT, String.join(" OR ", sessionParts));
		
		Boot.getLogger().log("ScrapeProducer on session names " + this.scraping_session_names + " is made.", true);
	}

	public ScrapeProducer(int startId, int endId) {
		super(String.format(ID_NAME, Boot.getScrapeTablename()), Scrape.class, Boot.getDatabaseConnectionPool(),
				BUFFER_SIZE, Boot.getLogger());
		
		this.setRowIDRange(startId, endId);
		Boot.getLogger().log("ScrapeProducer with ID range " + startId + " to " + endId + " is made.", true);
	}

	/**
	 * Creates a new provider that targets all scrapes from the database.
	 * @throws InterruptedException
	 
	public ScrapeProducer() throws InterruptedException {
		this(DEFAULT_SCRAPE_SESSIONS);
	}*/

	@Override
	protected String getUnboundedQuery() {
		// SELECT...FROM...JOIN
		String query = String.format(QUERY_BASE, Boot.getScrapeTablename());

		// ...WHERE...
		if (this.sessionIdSql != null)
			query += this.sessionIdSql;
		
		return query;
	}

	public String getScrapingSessionNames() {
		return scraping_session_names;
	}

}
