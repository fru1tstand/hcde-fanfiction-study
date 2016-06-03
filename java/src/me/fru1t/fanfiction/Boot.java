package me.fru1t.fanfiction;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import me.fru1t.fanfiction.database.producers.FandomProducer;
import me.fru1t.fanfiction.process.ScrapeProcess;
import me.fru1t.fanfiction.process.scrape.FandomPageUrlProducer;
import me.fru1t.util.DatabaseConnectionPool;
import me.fru1t.util.Logger;
import me.fru1t.web.MultiIPCrawler;

public class Boot {
	public static final boolean IS_RUNNING_LOCALLY = false;
	private static final boolean LOG_TO_FILE = true;
	public static final boolean DEBUG = false;

	// Log params
	private static final String LOG_FILE_PREFIX = "fanfiction-";
	private static final String LOG_FILE_SUFFIX = ".log";
	private static final SimpleDateFormat LOG_MESSAGE_PREFIX =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

	// Crawler params
	private static final int AVG_SLEEP_TIME_PER_IP = 3000;
	private static final String[] REMOTE_IPS = {
			"104.128.237.128",
			"104.128.233.73",
			"45.58.54.250",
//			"2602:ffc5:20::2:f686%venet0",
//			"2602:ffc5:20::2:93f0%venet0",
//			"2602:ffc5:20::2:c42%venet0",
//			"2602:ffc5:20::2:997c%venet0",
//			"2602:ffc5:20::2:c037%venet0",
//			"2602:ffc5:20::2:4cbe%venet0"
	};

	private static final int MIN_CONTENT_LENGTH = 1000;

	// Database params
	private static final String SQL_CONNECTION_STRING =
			"jdbc:mysql://local.fru1t.me/fanfiction?user=fanfiction&password=mypwISsoSecure!";
	private static final String LOCAL_SQL_CONNECTION_STRING =
			"jdbc:mysql://localhost/fanfiction?user=fanfiction&password=mypwISsoSecure!";

	private static Logger logger;
	private static MultiIPCrawler crawler;
	private static DatabaseConnectionPool dbcp;

	public static void main(String[] args) throws IOException, InterruptedException {
		crawler = null;
		dbcp = null;
		logger = new Logger();
		logger.logMessagePrefix(LOG_MESSAGE_PREFIX);

		if (!LOG_TO_FILE) {
			System.out.println("File logging disabled. To change this setting, edit Boot.java");
		} else {
			logger.logToFile(LOG_FILE_PREFIX, LOG_FILE_SUFFIX);
		}

//		(new ScrapeProcess(new CategoryPage(), "Category Pages 5-26-2016")).run();
//		(new ConvertProcess<ScrapeProducer.Scrape>(
//				new ScrapeProducer("Category Pages 5-26-2016"),
//				new CategoryToFandoms(),
//				"Category Pages 5-26-2016 - Convert")).run();
		(new ScrapeProcess(
				new FandomPageUrlProducer(new FandomProducer()),
				Session.SCRAPE_ALL_FANDOM_PAGES_16_6_1)).run();
	}

	public static Logger getLogger() {
		return logger;
	}

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

	public static DatabaseConnectionPool getDatabaseConnectionPool() {
		if (dbcp == null) {
			dbcp = new DatabaseConnectionPool(
					((IS_RUNNING_LOCALLY) ? LOCAL_SQL_CONNECTION_STRING : SQL_CONNECTION_STRING),
					logger);
		}
		return dbcp;
	}
}
