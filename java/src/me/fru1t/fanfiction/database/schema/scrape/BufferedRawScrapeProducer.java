package me.fru1t.fanfiction.database.schema.scrape;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.Database;
import me.fru1t.fanfiction.database.schema.Scrape;

/**
 * Provides a thread-safe interface for fetching raw scrapes from the fanfiction database. Due
 * to memory constraints and the size of the dataset, we can't simply fetch all scrapes,
 * so this class provides a means of queuing the raw scrapes from the database in a thread-safe
 * manner to allow multi-threaded processing.
 * 
 * This provider will always return scrapes from oldest to newest (lowest id to highest).
 */
public class BufferedRawScrapeProducer {
	private static final String QUERY_BASE =
			"SELECT"
			+ " `scrape_raw`.`id` AS `" + Scrape.ScrapeRaw.COLUMN_ID
			+ "`, `scrape_raw`.`scrape_session_id` AS `" + Scrape.ScrapeRaw.COLUMN_SCRAPE_SESSION_ID
			+ "`, `scrape_raw`.`date` AS `" + Scrape.ScrapeRaw.COLUMN_DATE
			+ "`, `scrape_raw`.`url` AS `" + Scrape.ScrapeRaw.COLUMN_URL
			+ "`, `scrape_raw`.`content` AS `" + Scrape.ScrapeRaw.COLUMN_CONTENT
			+ "` FROM `scrape_raw` ";
	private static final String QUERY_SESSION_NAME_JOIN =
			"INNER JOIN scrape_session ON scrape_session.id = scrape_raw.scrape_session_id ";
	private static final String QUERY_WHERE = "WHERE 1 = 1 ";
	private static final String QUERY_ORDER = "ORDER BY `scrape_raw`.`id` ASC " ;
	
	private static final String FMT_QUERY_SESSION_NAMES = "AND `scrape_session`.`name` IN ('%s') ";
	private static final String FMT_QUERY_LOWER_BOUND = "AND `scrape_raw`.`id` > %d ";
	private static final String FMT_QUERY_UPPER_BOUND = "AND `scrape_raw`.`id` < %d ";
	private static final String FMT_QUERY_LIMIT = "LIMIT %d";
	
	private static final int BUFFER_SIZE = 50;
	private static final int DEFAULT_BOUND_VALUE = -1;
	private static final String[] DEFAULT_SCRAPE_SESSIONS = {};
	
	private int lowerIdBound;
	private int upperIdBound;
	private String[] sessionNames;
	
	private int currentId;
	private boolean isComplete;
	private Queue<Scrape.ScrapeRaw> queue;
	
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
	 */
	public BufferedRawScrapeProducer(int lowerIdBound, int upperIdBound, String[] sessionNames) {
		this.lowerIdBound = (lowerIdBound < 0) ? -1 : lowerIdBound;
		this.upperIdBound = (upperIdBound < 0) ? -1 : upperIdBound;
		
		// Sanitize session names
		this.sessionNames = new String[sessionNames.length];
		for (int i = 0; i < sessionNames.length; i++) {
			this.sessionNames[i] = sessionNames[i].replaceAll("'", "\\'");
		}
		
		this.currentId = DEFAULT_BOUND_VALUE;
		this.isComplete = false;
		this.queue = new LinkedList<>();
	}
	
	/**
	 * Creates a new provider that only returns scrapes that belong to the given session names.
	 * 
	 * @param sessionNames
	 */
	public BufferedRawScrapeProducer(String... sessionNames) {
		this(DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE, sessionNames);
	}
	
	/**
	 * Creates a new provider that targets all scrapes from the database.
	 */
	public BufferedRawScrapeProducer() {
		this(DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE, DEFAULT_SCRAPE_SESSIONS);
	}
	
	/**
	 * Attempts to get the next raw scrape from the queue. This method is thread-safe and will
	 * block other threads until complete.
	 * 
	 * @return Raw scrape objects or NULL if there are none left.
	 */
	public synchronized Scrape.ScrapeRaw take() {
		// Elements still in queue
		if (!queue.isEmpty()) {
			return queue.poll();
		}
		
		// No elements in queue and already flagged as complete
		if (isComplete) {
			return null;
		}
		
		// No elements in queue and not complete
		refillQueue();
		if (!queue.isEmpty()) {
			return queue.poll();
		}
		
		// No elements in queue and none left in database
		isComplete = true;
		return null;
	}
	
	
	private String createQueryString() {
		// SELECT...FROM...JOIN
		String query = QUERY_BASE;
		if (sessionNames.length != 0) {
			query += QUERY_SESSION_NAME_JOIN;
		}
		
		// ...WHERE...
		query += QUERY_WHERE;
		if (sessionNames.length != 0) {
			query += String.format(FMT_QUERY_SESSION_NAMES, String.join("', '", sessionNames));
		}
		if (upperIdBound != DEFAULT_BOUND_VALUE) {
			query += String.format(FMT_QUERY_UPPER_BOUND, upperIdBound);
		}
		
		// Always have a lower bound, be it the lower bound specified by the user, or the currentId
		query += String.format(FMT_QUERY_LOWER_BOUND,
				(lowerIdBound > currentId) ? lowerIdBound : currentId);
		
		// ...ORDER BY...
		query += QUERY_ORDER;
		
		// ...LIMIT...
		query += String.format(FMT_QUERY_LIMIT, BUFFER_SIZE);
		
		return query;
	}
	
	private void refillQueue() {
		String query = createQueryString();
		try {
			Connection c = Database.getConnection();
			if (c == null) {
				Boot.log("Connection to the database failed when attempting to fill "
						+ "a BufferedRawScrapeProducer");
				return;
			}
			PreparedStatement stmt = c.prepareStatement(query);
			ResultSet result = stmt.executeQuery();
			Scrape.ScrapeRaw rawScrape;
			while (result.next()) {
				// Add result to queue
				rawScrape = new Scrape.ScrapeRaw();
				rawScrape.id = result.getInt(Scrape.ScrapeRaw.COLUMN_ID);
				rawScrape.scrapeSessionId =
						result.getInt(Scrape.ScrapeRaw.COLUMN_SCRAPE_SESSION_ID);
				rawScrape.date = result.getInt(Scrape.ScrapeRaw.COLUMN_DATE);
				rawScrape.url = result.getString(Scrape.ScrapeRaw.COLUMN_URL);
				rawScrape.content = result.getString(Scrape.ScrapeRaw.COLUMN_CONTENT);
				queue.add(rawScrape);
				
				// Set current Id to highest in result set
				if (rawScrape.id > currentId) {
					currentId = rawScrape.id;
				}
				
				rawScrape = null;
			}
			stmt.close();
		} catch (SQLException e) {
			Boot.log(e, null);
			Boot.log("Using Query: " + query);
		}
	}
}
