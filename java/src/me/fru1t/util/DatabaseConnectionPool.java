package me.fru1t.util;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * DatabaseConnectionPool provides fail-safe methods of execution for queries to a MySQL database.
 */
public class DatabaseConnectionPool {
	/**
	 * DatabaseConnectionPool.Statement is intended to be an anonymous class passed to any one of
	 * the methods contained within the DatabaseConnectionPool object for operation.
	 */
	public abstract static class Statement {
		/**
		 * Defines the execution behavior for this statement.
		 *
		 * @param c The database connection.
		 */
		public abstract void execute(Connection c) throws SQLException;
	}
	private static final int ERROR_UNKNOWN = 0;
	private static final int ERROR_BAD_HOST = 1042;
	private static final int ERROR_HANDSHAKE = 1043;
	private static final int ERROR_NO_DB = 1046;
	private static final int ERROR_FORCING_CLOSE = 1080;

	private static final int DEFAULT_MS_BEFORE_RETRYING = 5000;

	private int msBeforeRetry;
	private String dbConnectionString;
	/** @Nullable */
	private Connection connection;
	private Logger logger;

	private boolean useSSH;
	private String sshHost;
	private String sshUser;
	private String sshPass;
	private Session sshSession;

	/**
	 * Creates a new connection pool
	 */
	public DatabaseConnectionPool(String dbConnectionString, Logger logger) {
		this.msBeforeRetry = DEFAULT_MS_BEFORE_RETRYING;
		this.dbConnectionString = dbConnectionString;
		this.connection = null;
		this.logger = logger;

		this.sshHost = null;
		this.sshUser = null;
		this.sshPass = null;
		this.useSSH = false;
		this.sshSession = null;
		//justGetOnWithSSH();
		promptSSH();
	}

	/**
	 * Sets the duration to wait before retrying a failed (retryable) database operation.
	 *
	 * @param msBeforeRetry
	 * @return This.
	 */
	public DatabaseConnectionPool setRetryDelay(int msBeforeRetry) {
		this.msBeforeRetry = msBeforeRetry;
		return this;
	}

	/**
	 * Guarantees a database connection.
	 *
	 * @throws InterruptedException Thrown if an interrupt occurs before the connection
	 * was established.
	 */
	public synchronized Connection getConnection() throws InterruptedException {
		while (connection == null) {
			try {
				if (useSSH) {
					if (sshSession != null) {
						sshSession.disconnect();
					}
					connectSSH();
				}
			    
				connection = DriverManager.getConnection(dbConnectionString);
			} catch (SQLException e) {
				logger.log(e, "Couldn't connect to the database, retrying after delay...");
				connection = null;
				ThreadUtils.waitGauss(msBeforeRetry);
			}
		}
		return connection;
	}

	/**
	 * Guarantees the given statement will reach the database. The statement may run more than once
	 * if a network connectivity problem occurs.
	 *
	 * @throws InterruptedException Thrown if an interrupt occurs before the statement executed
	 * on the database.
	 */
	public synchronized void executeStatement(Statement stmt) throws InterruptedException {
		while (true) {
			try {
				Connection c = getConnection();
				stmt.execute(c);
				return;
			} catch (SQLException se) {
				// Skip if error wasn't network related
				if (!Preconditions.isAnyOf(se.getErrorCode(),
						ERROR_UNKNOWN, ERROR_BAD_HOST, ERROR_HANDSHAKE, ERROR_NO_DB, ERROR_FORCING_CLOSE)) {
					logger.log(se, "Skipping statement due to error code " + se.getErrorCode());
					return;
				}

				// Close connection to trigger a reconnect
				logger.log(se, "Reconnecting and waiting before retrying statement due to");
				try {
					connection.close();
				} catch (SQLException e) {
					logger.log(e, "When closing connection, caught: ");
				} finally {
					connection = null;
				}
				ThreadUtils.waitGauss(msBeforeRetry);
			}
		}
	}

	private void justGetOnWithSSH() {
		sshHost = "hdslab.hcde.washington.edu";
		sshUser = "jihyunl";
		// But let's ask for password at least...
		Console console = System.console();
		System.out.print("SSH Password: ");
		sshPass = String.valueOf(console.readPassword());
		connectSSH();
	}

	private void promptSSH() {
		// Ask if using SSH
		Scanner consoleScanner = new Scanner(System.in);
		System.out.print("Use SSH to connect to database [y/N]? ");

		try {
			if (consoleScanner.nextLine().toLowerCase().equals("y")) {
				/*useSSH = true;
				System.out.println();

				// The console object helps hide the password.
				// See: http://stackoverflow.com/questions/8138411/masking-password-input-from-the-console-java
				Console console = System.console();
				if (console == null) {
					logger.log("I couldn't get a console instance most likely because I'm "
							+ "running in  an IDE. WARNING: This means I will NOT hide your "
							+ "password when you type it into the console.", true);
				}

				// Fetch SSH information
				System.out.print("SSH Host: ");
				sshHost = consoleScanner.nextLine();

				System.out.print("SSH Username: ");
				sshUser = consoleScanner.nextLine();

				if (console == null) {
					System.out.print("SSH Password [WARNING: NOT HIDDEN]: ");
					sshPass = consoleScanner.nextLine();
				} else {
					System.out.print("SSH Password: ");
					sshPass = String.valueOf(console.readPassword());
				}
				connectSSH();*/
				justGetOnWithSSH();
			}
		} finally {
			consoleScanner.close();
		}
	}

	private void connectSSH() {
		try {
			JSch jsch = new JSch();
			sshSession = jsch.getSession(sshUser, sshHost, 22);
			sshSession.setPassword(sshPass);
			sshSession.setConfig("StrictHostKeyChecking", "no");
			sshSession.connect();

			// Sets up MySQL Port forwarding through the SSH tunnel
			sshSession.setPortForwardingL(3306, "localhost", 3306);
		} catch (JSchException e) {
			logger.log(e);
		}
	}
}
