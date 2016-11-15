package me.fru1t.fanfiction;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.stream.Stream;

import me.fru1t.fanfiction.Session.SessionName;
import me.fru1t.fanfiction.database.producers.ProfileProducer;
import me.fru1t.fanfiction.database.producers.ScrapeProducer;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.database.producers.StoryProducer;
import me.fru1t.fanfiction.process.BatchScrapeProcess;
import me.fru1t.fanfiction.process.BatchUserConvertProcess;
import me.fru1t.fanfiction.process.ConvertProcess;
import me.fru1t.fanfiction.process.ScrapeProcess;
import me.fru1t.fanfiction.process.convert.UserToProfiles;
import me.fru1t.fanfiction.process.scrape.ReviewPageUrlProducer;
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

	// Database params
	public static int LAST_DATE_PUBLISHED = 1476594560;
	public static int startid = 1, endid = 1;
	public static final String database = "fanfictiondrg201610";
	
	private static final String LOCAL_SQL_CONNECTION_STRING =
			"jdbc:mysql://localhost/" + database
			+ "?rewriteBatchedStatements=true"
			//+ "&useUnicode=true&characterEncoding=UTF-8" 
			+ "&user=fanfictiondrg&password=fanfictiondrg2016@HCDE";

	private static Logger logger;
	private static MultiIPCrawler crawler;
	private static DatabaseConnectionPool dbcp;

	public static void main(String[] args) throws IOException, InterruptedException {
		crawler = null;
		dbcp = null;
		logger = new Logger();
		logger.logMessagePrefix(LOG_MESSAGE_PREFIX);

		if (DEBUG) {
			System.out.println("File logging disabled due to debug mode. "
					+ "To change this setting, edit Boot.java");
		} else {
			logger.logToFile(LOG_FILE_PREFIX, LOG_FILE_SUFFIX);
		}
		
		if (args[0].equals("scrapeUser")) {
			
			REMOTE_IPS = IPs.getIPsetByName(args[1]);
			startid = Integer.parseInt(args[2]);
			endid = Integer.parseInt(args[3]);
			
			ProfileProducer profileProducer = new ProfileProducer();
			profileProducer.setRowIDRange(startid, endid);
			(new BatchScrapeProcess(
					new UserPageUrlProducer(profileProducer), 
					SessionName.SCRAPE_PROFILE_PAGES_16_11_12)).run();
			
		}  else if (args[0].equals("batchConvertUser")) {

			REMOTE_IPS = IPs.getIPsetByName(args[1]);
			startid = Integer.parseInt(args[2]);
			endid = Integer.parseInt(args[3]);
			
			ScrapeProducer scrapeProducer = 
					new ScrapeProducer(SessionName.SCRAPE_PROFILE_PAGES_16_10_18);
			scrapeProducer.setRowIDRange(startid, endid);
			(new BatchUserConvertProcess<Scrape>(
					scrapeProducer,
					SessionName.CONVERT_PROFILE_PAGES_16_11_10)).run();
			
		} else if (args[0].equals("scrapeReview")) {
			
			REMOTE_IPS = IPs.getIPsetByName(args[1]);
			startid = Integer.parseInt(args[2]);
			endid = Integer.parseInt(args[3]);
			
			StoryProducer storyProducer = new StoryProducer();
			storyProducer.setRowIDRange(startid, endid);
			storyProducer.setOtherWhereClause("`story`.`reviews` > 0");
			(new BatchScrapeProcess(
					new ReviewPageUrlProducer(storyProducer), 
					SessionName.SCRAPE_REVIEW_PAGES_16_11_09)).run();
			
		} else if (args[0].equals("convertReviewTempo")) {
			
			ScrapeProducer scrapeProducer = new ScrapeProducer(SessionName.STH);
			(new ConvertProcess<Scrape>(
					scrapeProducer,
					new UserToProfiles(),
					SessionName.STH)).run();
			
		} else if (args[0].equals("fileReview")) {
			
			REMOTE_IPS = IPs.getIPsetByName(args[1]);
			
			// scrape the urls that failed on batch inserts
			ArrayList<String> urls = new ArrayList<String>();
			try (Stream<String> stream = Files.lines(Paths.get("./" + args[2]))) {
		        stream.forEach(line -> urls.add(line));
			}
			(new ScrapeProcess(
					new TxtFileBasedUrlProducer(urls),
					SessionName.SCRAPE_REVIEW_PAGES_16_11_09)).run();
			
		}
		
	}
	
	public static Logger getLogger() {
		return logger;
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
			crawler
				.setMinContentLength(MIN_CONTENT_LENGTH);
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
