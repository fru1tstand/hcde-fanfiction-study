package me.fru1t.util;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.jdt.annotation.Nullable;

public class DatabaseUtils {
	private static final int ER_BAD_HOST_ERROR = 1042;
	private static final int ER_HANDSHAKE_ERROR = 1043;
	private static final int ER_NO_DB_ERROR = 1046;
	private static final int ER_FORCING_CLOSE = 1080;
	
	private static final int MILLISECONDS_BEFORE_RETRYING = 5000;
	
	/**
	 * Gracefully executes the given statement. Retries on any of network connection issues. Also
	 * closes the statement no matter what.
	 * 
	 * @param stmt
	 * @param logger
	 * @throws InterruptedException
	 */
	public static void executeStatement(CallableStatement stmt, Logger logger) throws InterruptedException {
		while (true) {
			try {
				stmt.execute();
				return;
			} catch (SQLException e) {
				if (!Preconditions.isAnyOf(e.getErrorCode(), ER_BAD_HOST_ERROR, ER_HANDSHAKE_ERROR, ER_NO_DB_ERROR, ER_FORCING_CLOSE)) {
					logger.log(e, "Skipping statement due to...");
					return;
				}
				
				logger.log(e, "Retrying statement due to...");
				ThreadUtils.waitGauss(MILLISECONDS_BEFORE_RETRYING);
			} finally {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.log(e, "Couldn't clean up resources due to ");
				}
			}
		}
	}
	
	/**
	 * Gracefully closes a given statement if it exists.
	 * 
	 * @param stmt
	 */
	public static void closeStatement(@Nullable Statement stmt, Logger logger) {
		if (stmt != null) {
			try {
				if (!stmt.isClosed()) {
					stmt.close();
				}
			} catch (SQLException e) {
				logger.log(e, "Couldn't close statement due to ");
			}
		}
	}
}
