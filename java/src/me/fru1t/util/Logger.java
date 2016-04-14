package me.fru1t.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.Nullable;

public class Logger {
	private SimpleDateFormat messagePrefix;
	private boolean logToFile;
	private BufferedWriter fileWriter;
	
	/**
	 * Creates a new logger.
	 */
	public Logger() {
		this.messagePrefix = null;
		this.logToFile = false;
		this.fileWriter = null;
	}
	
	/**
	 * Sets up logging to a file
	 * 
	 * @param fileNamePrefix
	 * @param fileNameSuffix
	 * @throws IOException
	 */
	public void logToFile(String fileNamePrefix, String fileNameSuffix) throws IOException {
		fileWriter = new BufferedWriter(new FileWriter(
				fileNamePrefix + (new Date()).getTime() + fileNameSuffix, true));
		logToFile = true;
	}
	
	/**
	 * Sets up message prefixing
	 * @param sdf
	 */
	public void logMessagePrefix(SimpleDateFormat sdf) {
		this.messagePrefix = sdf;
	}
	
	/**
	 * Logs a string message.
	 * 
	 * @param message
	 * @return
	 */
	public String log(String message) {
		// Log to console
		System.out.println(getLogStringPrefix() + message);
		
		// Log to file
		if (logToFile) {
			try {
				fileWriter.write(getLogStringPrefix() + message + "\r\n");
				fileWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(getLogStringPrefix()
						+ "Couldn't write to log file: "
						+ e.getMessage());
			}
		}

		return message;
	}
	
	/**
	 * Consumes an exception by adding it to the working log and returning the error that occurred.
	 * 
	 * @param e The exception to handle.
	 * @return A formatted user-friendly alert string.
	 */
	public String log(Exception e) {
		int check = (int) (Math.random() * Integer.MAX_VALUE);
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return log(errors.toString() + " [Error ID: " + check  + "]");
	}
	
	/**
	 * Consumes an exception by adding it to the working log and returning a formatted user-facing
	 * error string.
	 * 
	 * @param e The exception to handle.
	 * @param userMessage A user-friendly alert string.
	 * @return A formatted user-friendly alert string.
	 */
	public String log(Exception e, @Nullable String userMessage) {
		int check = (int) (Math.random() * Integer.MAX_VALUE);
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		return log("User message: " + userMessage + "\r\n"
				+ errors.toString() + " [Error ID: " + check  + "]");
	}
	
	private String getLogStringPrefix() {
		if (messagePrefix != null) {
			return "[" + messagePrefix.format(new Date()) + "] ";
		}
		
		return "";
	}
}
