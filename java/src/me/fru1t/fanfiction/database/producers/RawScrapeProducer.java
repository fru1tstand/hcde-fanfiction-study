package me.fru1t.fanfiction.database.producers;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.Database;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Provides a thread-safe interface for fetching raw scrapes from the fanfiction database. Due
 * to memory constraints and the size of the dataset, we can't simply fetch all scrapes,
 * so this class provides a means of queuing the raw scrapes from the database in a thread-safe
 * manner to allow multi-threaded processing.
 * 
 * This provider will always return scrapes from oldest to newest (lowest id to highest).
 * 
 * TODO (1): Add filtering by scrape_type in BufferedRawScrapeProducer
 */
public class RawScrapeProducer extends DatabaseProducer<RawScrapeProducer.Scrape, Integer> {
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
		
		/** url VARCHAR(255) */
		public String url;
		
		/** content MEDIUMTEXT*/
		public String content;
	}
	
	private static final int BUFFER_SIZE = 50;
	private static final String ID_NAME = "`scrape_raw`.`id`";
	private static final String QUERY_BASE =
			"SELECT"
			+ " `scrape`.`id` AS `" + Scrape.COLUMN_ID
			+ "`, `scrape`.`session_id` AS `" + Scrape.COLUMN_SESSION_ID
			+ "`, `scrape`.`date` AS `" + Scrape.COLUMN_DATE
			+ "`, `scrape`.`url` AS `" + Scrape.COLUMN_URL
			+ "`, `scrape`.`content` AS `" + Scrape.COLUMN_CONTENT
			+ "` FROM `scrape` ";
	private static final String QUERY_SESSION_NAME_JOIN =
			"INNER JOIN `session` ON `session`.`id` = `scrape`.`session_id` ";
	private static final String QUERY_WHERE = "WHERE 1 = 1 ";
	
	private static final String FMT_QUERY_SESSION_NAMES = "AND `session`.`name` IN ('%s') ";
	private static final String FMT_QUERY_LOWER_BOUND = "AND `scrape`.`id` > %d ";
	private static final String FMT_QUERY_UPPER_BOUND = "AND `scrape`.`id` < %d ";
	
	private static final int DEFAULT_BOUND_VALUE = -1;
	@Nullable
	private static final String[] DEFAULT_SCRAPE_SESSIONS = {};
	
	private int lowerIdBound;
	private int upperIdBound;
	private String[] sessionNames;
	
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
	public RawScrapeProducer(
			int lowerIdBound,
			int upperIdBound,
			@Nullable String[] sessionNames) {
		super(ID_NAME, Scrape.class, Database.getConnection(),
				BUFFER_SIZE, Boot.getLogger());
		this.lowerIdBound = (lowerIdBound < 0) ? -1 : lowerIdBound;
		this.upperIdBound = (upperIdBound < 0) ? -1 : upperIdBound;
		
		// Sanitize session names
		this.sessionNames = DEFAULT_SCRAPE_SESSIONS;
		if (sessionNames != null) {
			this.sessionNames = new String[sessionNames.length];
			for (int i = 0; i < sessionNames.length; i++) {
				String s = sessionNames[i];
				if (s != null) {
					this.sessionNames[i] = s.replaceAll("'", "\\'");
				}
			}
		}
	}
	
	/**
	 * Creates a new provider that only returns scrapes that belong to the given session names.
	 * 
	 * @param sessionNames
	 */
	public RawScrapeProducer(@Nullable String... sessionNames) {
		this(DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE, sessionNames);
	}
	
	/**
	 * Creates a new provider that targets all scrapes from the database.
	 */
	public RawScrapeProducer() {
		this(DEFAULT_BOUND_VALUE, DEFAULT_BOUND_VALUE, DEFAULT_SCRAPE_SESSIONS);
	}
	
	@Override
	protected String getUnboundedQuery() {
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
		if (lowerIdBound != DEFAULT_BOUND_VALUE) {
			query += String.format(FMT_QUERY_LOWER_BOUND, lowerIdBound);
		}
		
		return query;
	}
	

}
