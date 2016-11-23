package me.fru1t.util.concurrent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.DatabaseConnectionPool;
import me.fru1t.util.DatabaseConnectionPool.Statement;
import me.fru1t.util.Logger;
import me.fru1t.util.Producer;

/**
 * Provides methods that allow for sequential access to a database.
 *
 * @param <T> The type returned by this producer.
 * @param <I> The index type.
 */
public abstract class DatabaseProducer<T extends DatabaseProducer.Row<I>, I> extends Producer<T> {
	public static abstract class Row<I> {
		public static final String COLUMN_ID = "id";
		public I id;
	}
	
	public static class SelectRowIDRange {
		public int startId;
		public int endId;
		public SelectRowIDRange (int s, int e) {
			startId = s; endId = e;
		}
	}

	private static final String QUERY_ORDERBY = " ORDER BY %s ASC";
	private static final String QUERY_LIMIT = " LIMIT %d";
	private static final String QUERY_WHERE_GUARANTEE = " WHERE 1 = 1";
	private static final String QUERY_CURRENT_ID = " AND %s > %d";
	private static final String QUERY_WHERE_CHECK = " WHERE ";

	// Core class fields
	private int bufferSize;
	private String idName;
	private Boolean hasWhereClause;
	private DatabaseConnectionPool dbcp;

	private boolean isComplete;
	private Class<T> rowClass;
	private Queue<T> queue;
	@Nullable private I currentId;

	// Metrics/Logging fields
	private Logger logger;
	private long lastFetchTime;
	private long currentFetchTime;
	@Nullable private I firstScrapeId;
	@Nullable private I lastScrapeId;
	private int rowsProcessedSinceLastFetch;
	private int totalRowsProcessed;
	private SelectRowIDRange rowIDRange;
	private String otherWhereClause;
	
	public DatabaseProducer(
			String idName,
			Class<T> rowClass,
			DatabaseConnectionPool dbcp,
			int bufferSize,
			Logger logger) {
		this.idName = idName;
		this.dbcp = dbcp;
		this.rowClass = rowClass;
		this.isComplete = false;
		this.queue = new LinkedList<>();
		this.currentId = null;
		this.hasWhereClause = null;
		this.bufferSize = bufferSize;

		this.logger = logger;
		this.firstScrapeId = null;
		this.lastScrapeId = null;
		this.lastFetchTime = 0;
		this.currentFetchTime = 0;
		this.rowsProcessedSinceLastFetch = 0;
		this.totalRowsProcessed = 0;
		this.rowIDRange = null;
		this.otherWhereClause = null;
	}

	/**
	 * Forces the producer to only return rows with an ID "greater than" the given value.
	 *
	 * @param id
	 * @return This.
	 */
	public DatabaseProducer<T, I> startAt(I id) {
		this.currentId = id;
		return this;
	}
	
	/**
	 * Set the range of row ids to be considered for producer.
	 */
	public void setRowIDRange(int start, int end) {
		rowIDRange = new SelectRowIDRange(start, end);
	}
	
	public void setOtherWhereClause(String str) {
		otherWhereClause = str;
	}

	/**
	 * Thread-safely returns the next row from the table, or null if none are left.
	 *
	 * @return The next row from the table, or null if none are left.
	 */
	@Override
	@Nullable
	public final synchronized T take() {
		if (totalRowsProcessed % 100 == 0 && !queue.isEmpty()) {
			logger.log("Regular Report: Procssed " 
					+ totalRowsProcessed 
					+ " rows so far, currently on id "
					+ queue.peek().id, true);
		}
		
		// Queue still has stuff
		if (!queue.isEmpty()) {
			rowsProcessedSinceLastFetch++;
			totalRowsProcessed++;
			lastScrapeId = queue.peek().id;
			return queue.poll();
		}

		// No elements in queue and complete
		if (isComplete) {
			return null;
		}

		// No elements in queue and not complete
		currentFetchTime = (new Date()).getTime();
		if (lastFetchTime != 0) {
			logger.log("Processed "
					+ rowsProcessedSinceLastFetch
					+ " rows, between ["
					+ ((firstScrapeId != null) ? firstScrapeId.toString() : "null")
					+ ", "
					+ ((lastScrapeId != null) ? lastScrapeId.toString() : "null")
					+ "], in "
					+ (currentFetchTime - lastFetchTime)
					+ "ms", true);
		}
		lastFetchTime = currentFetchTime;
		rowsProcessedSinceLastFetch = 0;
		refillQueue();

		if (!queue.isEmpty()) {
			rowsProcessedSinceLastFetch++;
			totalRowsProcessed++;
			firstScrapeId = queue.peek().id;
			return queue.poll();
		}

		// No elements in queue and none left in database
		isComplete = true;
		return null;
	}

	@SuppressWarnings("unchecked")
	private void refillQueue() {
		String query = getQuery();
		try {
			dbcp.executeStatement(new Statement() {
				@Override
				public void execute(Connection c) throws SQLException {
					try {
						PreparedStatement stmt = c.prepareStatement(query);
						ResultSet result = stmt.executeQuery();
						T row;
						while (result.next()) {
							row = rowClass.newInstance();

							// Set the ID to the generic type passed to us. This method gets around
							// Java's type erasure for generics.
							rowClass
								.getField(Row.COLUMN_ID)
								.set(row, result.getObject(Row.COLUMN_ID,
										(Class<I>) ((ParameterizedType) rowClass
												.getGenericSuperclass())
											.getActualTypeArguments()[0]));

							// Set the remaining fields through normal reflection as they're not
							// parameterized
							for (Field field : rowClass.getDeclaredFields()) {
								if ((field.getModifiers() & Modifier.STATIC) == 0) {
									field.set(row,
											result.getObject(field.getName(), field.getType()));
								}
							}

							queue.add(row);
							currentId = row.id;
						}
						stmt.close();
						result.close();
					} catch (InstantiationException | IllegalAccessException
							| IllegalArgumentException | NoSuchFieldException
							| SecurityException e) {
						// These exceptions would only be thrown if an error in the programming
						// allowed it to be so. They're also non-recoverable.
						throw new RuntimeException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			// Interrupt occurred, dump everything and stop.
			logger.log(e, "Interrupt occured when refilling the DatabaseProducer's queue.");
			queue = new LinkedList<>();
		}

	}

	private String getQuery() {
		StringBuilder query = new StringBuilder(getUnboundedQuery());

		if (hasWhereClause == null) {
			hasWhereClause = getUnboundedQuery().contains(QUERY_WHERE_CHECK);
		}

		// ...WHERE... -- Guarantee the start of the where clause
		if (!hasWhereClause) {
			query.append(QUERY_WHERE_GUARANTEE);
		}
		
		if (rowIDRange != null) {
			if (rowIDRange.startId > 0)
				query.append(" AND " + idName + " >= " + rowIDRange.startId);
			
			if (rowIDRange.endId > 0) 
				query.append(" AND " + idName + " <= " + rowIDRange.endId);
		}

		// AND id > current
		if (currentId != null) {
			query.append(String.format(QUERY_CURRENT_ID, idName, currentId));
		}
		
		if (otherWhereClause != null && !otherWhereClause.equals("")) {
			query.append(" AND " + otherWhereClause);
		}

		// ...ORDER BY...
		query.append(String.format(QUERY_ORDERBY, idName));

		// ...LIMIT...
		query.append(String.format(QUERY_LIMIT, bufferSize));

		Boot.getLogger().log("DatabaseProducer getQuery : " + query.toString(), true);
		return query.toString();
	}

	/**
	 * This method should return an SQL query that defines the scope of the implementing producer.
	 * This could be as simple as selecting an entire table, to having joins, sub queries, etc.
	 * The query cannot have an ORDER BY clause or a LIMIT clause, and must define columns AS their
	 * java field counterparts.
	 *
	 * @return An SQL query.
	 */
	abstract protected String getUnboundedQuery();

	public boolean isComplete() {
		// TODO Auto-generated method stub
		return isComplete;
	}
}
