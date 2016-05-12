package me.fru1t.fanfiction.database.producers;

import java.sql.Connection;

import me.fru1t.util.Logger;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * TODO: Fix before use.
 * @deprecated
 */
public class StoryForHeatMapProducer extends DatabaseProducer<StoryForHeatMapProducer.Story, Integer> {
	public static class Story extends DatabaseProducer.Row<Integer> {}
	
	private static final int QUEUE_SIZE = 100000;
	private static final String COL_ID = "`ff_book_id`";
	
	public StoryForHeatMapProducer(Connection con, Logger logger) {
		super(COL_ID, Story.class, con, QUEUE_SIZE, logger);
	}

	@Override
	protected String getUnboundedQuery() {
		return "SELECT DISTINCT `ff_book_id` AS `id` FROM `scrape_book_result_ff_genre`";
	}
}
