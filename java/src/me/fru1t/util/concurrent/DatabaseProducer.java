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
import me.fru1t.util.Logger;

public abstract class DatabaseProducer<T extends DatabaseProducer.Row<I>, I> {
	public static abstract class Row<I> {
		public static final String COLUMN_ID = "id";
		public I id;
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
	private Connection connection;
	
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
	
	public DatabaseProducer(
			String idName,
			Class<T> rowClass,
			Connection connection,
			int bufferSize,
			Logger logger) {
		this.idName = idName;
		this.connection = connection;
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
	}
	
	/**
	 * Thread-safely returns the next row from the table, or null if none are left.
	 * 
	 * @return The next row from the table, or null if none are left.
	 */
	@Nullable
	public final synchronized T take() {
		// Queue still has stuff
		if (!queue.isEmpty()) {
			rowsProcessedSinceLastFetch++;
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
					+ " scrapes, between ["
					+ ((firstScrapeId != null) ? firstScrapeId.toString() : "null")
					+ ", "
					+ ((lastScrapeId != null) ? lastScrapeId.toString() : "null")
					+ "], in "
					+ (currentFetchTime - lastFetchTime)
					+ "ms");
		}
		lastFetchTime = currentFetchTime;
		rowsProcessedSinceLastFetch = 0;
		refillQueue();
		if (!queue.isEmpty()) {
			rowsProcessedSinceLastFetch++;
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
			PreparedStatement stmt = connection.prepareStatement(query);
			ResultSet result = stmt.executeQuery();
			T row;
			while (result.next()) {
				row = rowClass.newInstance();
				
				// Set the ID to the generic type passed to us. This method gets around Java's type
				// erasure for generics.
				rowClass.getField(Row.COLUMN_ID).set(row, result.getObject(Row.COLUMN_ID, 
						(Class<I>) ((ParameterizedType) rowClass.getGenericSuperclass())
								.getActualTypeArguments()[0]));
				
				// Set the remaining fields through normal reflection as they're not parameterized
				for (Field field : rowClass.getDeclaredFields()) {
					if ((field.getModifiers() & Modifier.STATIC) == 0) {
						field.set(row, result.getObject(field.getName(), field.getType()));
					}
				}
				
				queue.add(row);
				currentId = row.id;
			}
			stmt.close();
			result.close();
		} catch (SQLException e) {
			logger.log(e, "Using query: " + query);
		} catch (InstantiationException | IllegalAccessException e) {
			// This should never happen as the class is defined within this file
			throw new RuntimeException(e);
		} catch (IllegalArgumentException | NoSuchFieldException | SecurityException e) {
			Boot.getLogger().log(e);
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
		
		// AND id > current
		if (currentId != null) {
			query.append(String.format(QUERY_CURRENT_ID, idName, currentId));
		}
		
		// ...ORDER BY...
		query.append(String.format(QUERY_ORDERBY, idName));
		
		// ...LIMIT...
		query.append(String.format(QUERY_LIMIT, bufferSize));
		
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
}
