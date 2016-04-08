package me.fru1t.fanfiction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.process.ExtractBooksListDataProcess;

public class Boot {
	public static void main(String[] args) {
		if (!LOG_TO_FILE) {
			System.out.println("File logging disabled. To change this setting, edit Boot.java");
		}
		try {
			(new ExtractBooksListDataProcess()).run();
		} catch (Exception e) {
			log(e, null);
		}
	}

	private static final boolean LOG_TO_FILE = true;
	private static final String LOG_PREFIX = "fanfiction-";
	private static final SimpleDateFormat dateFormat =
			new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
	private static BufferedWriter logWriter = null;
	
	/**
	 * Adds an entry to the working log of this session.
	 * 
	 * @param message The message to add.
	 * @return The same incoming message.
	 */
	public static final String log(String message) {
		// Log to console
		System.out.println(getTimestampPrefix() + message);
		
		// Log to file
		if (LOG_TO_FILE) {
			if (logWriter == null) {
				try {
					System.out.println(getTimestampPrefix() + "Creating new log file");
					logWriter = new BufferedWriter(new FileWriter(
							LOG_PREFIX + (new Date()).getTime() + ".log", true));
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println(getTimestampPrefix()
							+ "Couldn't create log file: "
							+ e.getMessage());
					return message;
				}
			}
			
			try {
				logWriter.write(getTimestampPrefix() + message + "\r\n");
				logWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(getTimestampPrefix()
						+ "Couldn't write to log file: "
						+ e.getMessage());
			}
		}

		return message;
	}

	/**
	 * Consumes an exception by adding it to the working log and returning a formatted user-facing
	 * error string.
	 * 
	 * @param e The exception to handle.
	 * @param userMessage A user-friendly alert string.
	 * @return A formatted user-friendly alert string.
	 */
	public static final String log(Exception e, @Nullable String userMessage) {
		int check = (int) (Math.random() * Integer.MAX_VALUE);
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		Boot.log("User message: " + userMessage + "\r\n" + errors.toString() + " [" + check  + "]");
		return ((userMessage == null) ? "" : userMessage) + " [Error ID: " + check + "]";
	}
	
	private static final String getTimestampPrefix() {
		return "[" + dateFormat.format(new Date()) + "] ";
	}
}
