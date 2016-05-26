package me.fru1t.fanfiction.database.producers;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Produces Story ID and number of chapters that story contains.
 */
public class StoryIdAndChaptersProducer extends DatabaseProducer<StoryIdAndChaptersProducer.StoryIdAndChapters, Integer> {
	public static class StoryIdAndChapters extends DatabaseProducer.Row<Integer> {
		public int storyId;
		public int chapters;
	}

	private static final String ID_NAME = "`story`.`id`";

	private static final String RESTRICTED_QUERY = "SELECT"
			+ " `story`.`id` AS `id`,"
			+ " `story`.`ff_story_id` AS `storyId`,"
			+ " `story`.`chapters` AS `chapters`"
			+ " FROM `story`"
			+ " INNER JOIN `fandom` ON `fandom`.`id` = `story`.`fandom_id`"
			+ " WHERE `fandom`.`name` = 'Harry Potter '";

	public StoryIdAndChaptersProducer() throws InterruptedException {
		super(ID_NAME, StoryIdAndChapters.class, Boot.getDatabaseConnectionPool().getConnection(), 300, Boot.getLogger());
	}

	@Override
	protected String getUnboundedQuery() {
		return RESTRICTED_QUERY;
	}

}
