package me.fru1t.fanfiction;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import me.fru1t.fanfiction.database.producers.ProfileProducer;
import me.fru1t.fanfiction.process.ScrapeProcess;
import me.fru1t.fanfiction.process.scrape.ProfilePageUrlProducer;
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
	private static final int AVG_SLEEP_TIME_PER_IP = 1300;
	private static final String[] REMOTE_IPS = {
			"104.128.237.128",
			"104.128.233.73",
			"45.58.54.250"
			//"128.208.219.12" // lab server
	};

	private static final int MIN_CONTENT_LENGTH = 1000;

	// Database params
	private static final String LOCAL_SQL_CONNECTION_STRING =
			"jdbc:mysql://localhost/fanfictiondrg201610"
			+ "?user=fanfictiondrg&password=fanfictiondrg2016@HCDE";

//	private static final String LOCAL_SQL_CONNECTION_STRING =
//			"jdbc:mysql://localhost/newschema?user=root";

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

		ProfileProducer profileProducer = new ProfileProducer();
		profileProducer.startAt(2500); // ID 2543 gave an OOM exception. Backtracking to verify fix
		(new ScrapeProcess(
				new ProfilePageUrlProducer(profileProducer),
				Session.SCRAPE_PROFILE_PAGES_16_10_18)).run();

//		(new ConvertProcess<Scrape>(
//			new ScrapeProducer(Session.SCRAPE_PROFILE_PAGES_16_10_15),
//			new ProfileToUsers(),
//			Session.CONVERT_PROFILE_PAGES_16_10_15)).run();
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
