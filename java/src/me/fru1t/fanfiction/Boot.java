package me.fru1t.fanfiction;

import java.io.IOException;
import java.text.SimpleDateFormat;

import me.fru1t.fanfiction.process.FixMetadataProcess;
import me.fru1t.util.Logger;

public class Boot {
	private static final boolean LOG_TO_FILE = true;
	private static final String LOG_FILE_PREFIX = "fanfiction-";
	private static final String LOG_FILE_SUFFIX = ".log";
	private static final SimpleDateFormat LOG_MESSAGE_PREFIX =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	private static Logger logger;
	
	public static void main(String[] args) throws IOException {
		logger = new Logger();
		logger.logMessagePrefix(LOG_MESSAGE_PREFIX);
		
		
		if (!LOG_TO_FILE) {
			System.out.println("File logging disabled. To change this setting, edit Boot.java");
		} else {
			logger.logToFile(LOG_FILE_PREFIX, LOG_FILE_SUFFIX);
		}
		(new FixMetadataProcess()).run();
	}
	
	public static Logger getLogger() {
		return logger;
	}
}
