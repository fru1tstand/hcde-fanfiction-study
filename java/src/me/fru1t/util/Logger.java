package me.fru1t.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;

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
	public String log(String message, boolean toConsole) {
		// Log to console
		if (toConsole) System.out.println(getLogStringPrefix() + message);

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
		return log(errors.toString() + " [Error ID: " + check  + "]", true);
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
				+ errors.toString() + " [Error ID: " + check  + "]", true);
	}

	/**
	 * Submits an anonymous debug dialogue to the console.
	 *
	 * @param debugMessage The debug message.
	 */
	public void debug(@Nullable String debugMessage) {
		log("[DEBUG-Unknown] " + debugMessage, true);
	}

	/**
	 * Submits a named debug dialogue to the console.
	 *
	 * @param debugMessage The debug message.
	 * @param callingClass The class to display in the debug hint.
	 */
	public void debug(@Nullable String debugMessage, Class<?> callingClass) {
		log("[DEBUG-" + callingClass.getSimpleName() + "] " + debugMessage, true);
	}

	private String getLogStringPrefix() {
		if (messagePrefix != null) {
			return "[" + messagePrefix.format(new Date()) + "] ";
		}

		return "";
	}
	
	public static <E> void writeToFile(Exception e, String funcName, String prefix, Iterable<E> list) {
		String filename = prefix + (new Date()).getTime() + ".txt";

		Boot.getLogger().log(e, funcName + " is having trouble! " 
				+ "Outputting a list of urls that were supposed to be inserted to a file \""
				+ filename);

		try {
			BufferedWriter fileWriter = 
					new BufferedWriter(new FileWriter(filename, true));
			for (E myurl : list) {
				fileWriter.write(myurl + "\r\n");
				fileWriter.flush();
			}
			fileWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Boot.getLogger().log(e1, "Error with Couldn't write to file \"" + filename);
		}
	}
}
