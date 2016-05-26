package me.fru1t.fanfiction;

import java.io.IOException;
import java.text.SimpleDateFormat;

import me.fru1t.util.DatabaseConnectionPool;
import me.fru1t.util.Logger;
import me.fru1t.web.MultiIPCrawler;

public class Boot {
	public static final boolean IS_RUNNING_LOCALLY = false;

	// Log params
	private static final boolean LOG_TO_FILE = false;
	private static final String LOG_FILE_PREFIX = "fanfiction-";
	private static final String LOG_FILE_SUFFIX = ".log";
	private static final SimpleDateFormat LOG_MESSAGE_PREFIX =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

	// Crawler params
	private static final int AVG_SLEEP_TIME_PER_IP = 7500;
	private static final byte[][] LOCAL_IPS = {
			{ (byte) 192, (byte) 168, (byte) 1, (byte) 1 }
	};
	private static final byte[][] REMOTE_IPS = {
			{ (byte) 104, (byte) 128, (byte) 237, (byte) 128 },
			{ (byte) 104, (byte) 128, (byte) 233, (byte) 73 },
			{ (byte) 45, (byte) 58, (byte) 54, (byte) 250 }
	};

	// Database params
	private static final String SQL_CONNECTION_STRING =
			"jdbc:mysql://local.fru1t.me/fanfiction?user=fanfiction&password=mypwISsoSecure";
	private static final String LOCAL_SQL_CONNECTION_STRING =
			"jdbc:mysql://localhost/fanfiction?user=fanfiction&password=mypwISsoSecure";

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

//		(new ScrapeStoryContentFromStoriesProcess()).run();
	}

	public static Logger getLogger() {
		return logger;
	}

	public static MultiIPCrawler getCrawler() {
		if (crawler == null) {
			crawler = new MultiIPCrawler(logger, AVG_SLEEP_TIME_PER_IP, (IS_RUNNING_LOCALLY ? LOCAL_IPS : REMOTE_IPS));
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
