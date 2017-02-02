package me.fru1t.fanfiction;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Stream;

import me.fru1t.fanfiction.database.producers.FandomProducer;
import me.fru1t.fanfiction.database.producers.ScrapeProducer;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.database.producers.StoryProducer;
import me.fru1t.fanfiction.process.BatchReviewConvertProcess;
import me.fru1t.fanfiction.process.BatchScrapeProcess;
import me.fru1t.fanfiction.process.BatchStoryConvertProcess;
import me.fru1t.fanfiction.process.BatchUserConvertProcess;
import me.fru1t.fanfiction.process.ConvertProcess;
import me.fru1t.fanfiction.process.ScrapeProcess;
import me.fru1t.fanfiction.process.convert.CategoryToFandoms;
import me.fru1t.fanfiction.process.convert.FandomToStories;
import me.fru1t.fanfiction.process.scrape.CategoryPageUrlProducer;
import me.fru1t.fanfiction.process.scrape.FandomPageUrlProducer;
import me.fru1t.fanfiction.process.scrape.ReviewPageUrlProducer;
import me.fru1t.fanfiction.process.scrape.StoryPageUrlProducer;
import me.fru1t.fanfiction.process.scrape.TxtFileBasedUrlProducer;
import me.fru1t.fanfiction.process.scrape.UserPageUrlProducer;
import me.fru1t.util.DatabaseConnectionPool;
import me.fru1t.util.Logger;
import me.fru1t.web.MultiIPCrawler;

public class Boot {
	public static final boolean IS_RUNNING_LOCALLY = false;
	public static final boolean DEBUG = false;

	// changed boot
	// Log params
	private static final String LOG_FILE_PREFIX = "fanfiction-";
	private static final String LOG_FILE_SUFFIX = ".log";
	private static final SimpleDateFormat LOG_MESSAGE_PREFIX =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

	// Crawler params
	private static final int AVG_SLEEP_TIME_PER_IP = 1000;
	private static final int MIN_CONTENT_LENGTH = 1000;
	private static String[] REMOTE_IPS = null;
	
	// input params
	private static String server_name = null;
	private static String command = null;
	private static String scrape_tablename = null;
	private static Session session_of_this_run = null;
	
	// Database params
	private static final String database = "fanfictiondrg201605";
	private static final String LOCAL_SQL_CONNECTION_STRING =
			"jdbc:mysql://localhost/" + database
			+ "?rewriteBatchedStatements=true"
			//+ "&useUnicode=true&characterEncoding=UTF-8" 
			+ "&user=fanfictiondrg&password=fanfictiondrg2016@HCDE";

	private static Logger logger;
	private static MultiIPCrawler crawler;
	private static DatabaseConnectionPool dbcp;

	/**
	 * Three types of commands possible : 
	 * 1) java -jar *.jar [server_name] FILE_SCRAPE [scrape_tablename] [filename]
	 * 2) java -jar *.jar [server_name] SCRAPE_[STH] [scrape_tablename] [start_id] [end_id]
	 * 3) java -jar *.jar [server_name] CONVERT_[STH] [scrape_tablename] [scrape_session_name]
	 * 
	 * To avoid Heap Memory Error, specify `-Xms2048m -Xmx2048m` params before `-jar`
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		if (args[0].equals("options")) {
			System.out.println("1) java -jar *.jar [server_name] FILE_SCRAPE [scrape_tablename] [filename]");
			System.out.println("2) java -jar *.jar [server_name] SCRAPE_[STH] [scrape_tablename] [start_id] [end_id]");
			System.out.println("3) java -jar *.jar [server_name] CONVERT_[STH] [scrape_tablename] [scrape_session_name]");
			return;
		}
		
		crawler = null;
		dbcp = null;
		logger = new Logger();
		logger.logMessagePrefix(LOG_MESSAGE_PREFIX);

		if (DEBUG) System.out.println("File logging disabled due to debug mode. "
					+ "To change this setting, edit Boot.java");
		else logger.logToFile(LOG_FILE_PREFIX, LOG_FILE_SUFFIX);
		
		
		// these params must be specified for all incidents
		server_name = args[0]; REMOTE_IPS = IPs.getIPsetByName(server_name);
		command = args[1];
		scrape_tablename = args[2];
		
		if (command.equals("FILE_SCRAPE")) {
			String filename = args[3];
			session_of_this_run = new Session(String.format("%s_ON_%s", command, filename));
			handleScrapeByFile(filename);
			return;
		}

		String[] parts = command.split("_");
		if (parts.length != 2) {
			System.out.println("Invalid Command: " + command);
			return;
		}
		
		if (parts[0].equals("SCRAPE") && args.length == 5) {
			int start_id = Integer.parseInt(args[3]);
			int end_id = Integer.parseInt(args[4]);
			
			session_of_this_run = new Session(
				   String.format("%s_%s_%s", command, 
											(start_id < 1000 ? start_id : start_id/1000 + "K"), 
											(end_id < 1000 ? end_id : end_id/1000 + "K")));
			if (parts[1].equals("USER")) {
				// e.g. https://www.fanfiction.net/u/12345
				// ProfileProducer profileProducer = new ProfileProducer(start_id, end_id);
				(new BatchScrapeProcess(new UserPageUrlProducer(start_id, end_id, 8520203))).run();
			} else if (parts[1].equals("CATEGORY")) {
				// start_id and end_id are meaningless b/c categories are hard-coded.
				(new ScrapeProcess(new CategoryPageUrlProducer())).run();
			} else if (parts[1].equals("FANDOM")) {
				// scrape all lists of story on each fandom page.
				// e.g. https://www.fanfiction.net/book/Harry-Potter/?&srt=1&r=103&p=2
				FandomProducer fandomProducer = new FandomProducer(start_id, end_id);
				(new BatchScrapeProcess(new FandomPageUrlProducer(fandomProducer))).run();
			} else  if (parts[1].equals("REVIEW")) {
				// e.g. https://www.fanfiction.net/r/1425634/1/1/
				StoryProducer storyProducer = new StoryProducer(start_id, end_id);
				storyProducer.setOtherWhereClause("`story`.`reviews` > 0");
				(new BatchScrapeProcess(new ReviewPageUrlProducer(storyProducer))).run();
			} else if (parts[1].equals("STORY")) {
				// e.g. https://www.fanficiton.net/s/1425634
				StoryProducer storyProducer = new StoryProducer(start_id, end_id);
				(new BatchScrapeProcess(new StoryPageUrlProducer(storyProducer))).run();
			}
			
			return;
		}
		
		if (parts[0].equals("CONVERT")) {
			ScrapeProducer scrapeProducer = null;
			if (args.length == 4) {
				// get the scrapes with a session_id that matches scrape_session_name in `session` table
				String scrape_session_name = args[3];
				session_of_this_run = new Session(String.format("%s_ON_%s", command, scrape_session_name));
				scrapeProducer = new ScrapeProducer(scrape_session_name);
			} else if (args.length == 5) { 
				// we are given start_id and end_id of scrape_table,
				// get the scrapes withing this id range.
				int start_id = Integer.parseInt(args[3]);
				int end_id = Integer.parseInt(args[4]);
				session_of_this_run = new Session(
						   String.format("%s_ON_ID_%s_%s", command, 
													(start_id < 1000 ? start_id : start_id/1000 + "K"), 
													(end_id < 1000 ? end_id : end_id/1000 + "K")));
				
				scrapeProducer = new ScrapeProducer(start_id, end_id);
			} else {
				return;
			}
			 
			if (parts[1].equals("USERPROFILE")) {
				(new BatchUserConvertProcess<Scrape>(scrapeProducer, true)).run();
			} else if (parts[1].equals("USERFAVORITE")) {
				(new BatchUserConvertProcess<Scrape>(scrapeProducer, false)).run();
			} else if (parts[1].equals("CATEGORY")) { // EXTRACT fandom from category
				// get all fandoms for each category
				(new ConvertProcess<Scrape>(scrapeProducer, new CategoryToFandoms())).run();
				
			} else if (parts[1].equals("FANDOM")) { // EXTRACT story list from each fandom page
				// process the scraped story lists, and insert the story meta-data
				System.out.println("\n\t\t[ [ [ [ [ WARNING: make sure all urls in `fandom` table are unique. ] ] ] ] ]\n");
				(new ConvertProcess<Scrape>(scrapeProducer, new FandomToStories())).run();
				
			} else if (parts[1].equals("REVIEW")) {
				// as I process review page, I will simultaneously update reviewers to the `user` table 
				(new BatchReviewConvertProcess<Scrape>(scrapeProducer)).run();
				
			} else if (parts[1].equals("STORY")) {
				(new BatchStoryConvertProcess<Scrape>(scrapeProducer)).run();
			}
			return;
		}
	}
	
	private static void handleScrapeByFile(String filename) throws InterruptedException, IOException {
		// scrape the urls that failed on batch inserts
		ArrayList<String> urls = new ArrayList<String>();
		try (Stream<String> stream = Files.lines(Paths.get("./" + filename))) {
	        stream.forEach(line -> urls.add(line));
		}
		
		(new ScrapeProcess(new TxtFileBasedUrlProducer(urls))).run();
	}
	
	public static Logger getLogger() {
		return logger;
	}
	
	public static String getServerName() {
		return server_name;
	}
	
	public static String getCommand() {
		return command;
	}
	
	public static String getScrapeTablename() {
		return scrape_tablename;
	}
	
	public static Session getSessionOfThisRun() {
		return session_of_this_run;
	}
	
	public static String getDatabase() {
		return database;
	}

	/**
	 * Lazy load the multi-ip crawler.
	 *
	 * @return The singleton multi-ip crawler
	 */
	public static MultiIPCrawler getCrawler() {
		if (crawler == null) {
			byte[][] ips = null;
			try {
				if (IS_RUNNING_LOCALLY) {
						ips = new byte[][] { InetAddress.getLocalHost().getAddress() };
				} else {
					ips = new byte[REMOTE_IPS.length][];
					for (int i = 0; i < REMOTE_IPS.length; i++) {
						ips[i] = InetAddress.getByName(REMOTE_IPS[i]).getAddress();
					}
				}
			} catch (UnknownHostException e) {
				// If we can't get the host, something is very wrong.
				throw new RuntimeException(e);
			}
			crawler = new MultiIPCrawler(logger, AVG_SLEEP_TIME_PER_IP, ips);
			crawler.setMinContentLength(MIN_CONTENT_LENGTH);
		}
		return crawler;
	}

	/**
	 * Lazy load the database connection pool
	 *
	 * @return The singleton databaseconnectionpool.
	 */
	public static DatabaseConnectionPool getDatabaseConnectionPool() {
		if (dbcp == null) {
			dbcp = new DatabaseConnectionPool(LOCAL_SQL_CONNECTION_STRING, getLogger());
		}
		return dbcp;
	}
}
