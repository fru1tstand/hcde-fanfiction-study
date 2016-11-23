package me.fru1t.fanfiction.database.producers;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.concurrent.DatabaseProducer;

public class StoryProducer extends DatabaseProducer<StoryProducer.Story, Integer> {
	public static class Story extends DatabaseProducer.Row<Integer> {
		public int ff_story_id;
		public int fandom_id;
		public int reviews;
		public int chapters;

		@Override
		public String toString() {
			return "Story [ff_story_id=" + ff_story_id + ", fandom_id=" + fandom_id + 
					", reviews=" + reviews + ", chapters=" + chapters + "]";
		}
	}
	
	private static final String ID_NAME = "`story`.`id`";
	private static final int BUFFER_SIZE = 10000;
	
	public StoryProducer() {
		super(ID_NAME, Story.class, Boot.getDatabaseConnectionPool(), BUFFER_SIZE, Boot.getLogger());
		
		Boot.getLogger().log("StoryProducer with no ids range is made.", true);
	}
	
	public StoryProducer(int startid, int endid) {
		super(ID_NAME, Story.class, Boot.getDatabaseConnectionPool(), BUFFER_SIZE, Boot.getLogger());
		this.setRowIDRange(startid, endid);
		
		Boot.getLogger().log("StoryProducer with ids range " + startid + " to " + endid + " is made.", true);
	}

	@Override
	protected String getUnboundedQuery() {
		return "SELECT"
				+ " `story`.`id` AS `id`,"
				+ " `story`.`ff_story_id` AS `ff_story_id`,"
				+ " `story`.`fandom_id` AS `fandom_id`,"
				+ " `story`.`reviews` AS `reviews`,"
				+ " `story`.`chapters` AS `chapters`"
				+ " FROM `story`";
	}
	
	
}