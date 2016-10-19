package me.fru1t.fanfiction.database.producers;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.concurrent.DatabaseProducer;

public class FandomProducer extends DatabaseProducer<FandomProducer.Fandom, Integer> {
	/**
	 * Represents a Fandom.
	 */
	public static class Fandom extends DatabaseProducer.Row<Integer> {
		public String category;
		public String name;
		public String url;

		@Override
		public String toString() {
			return "Fandom [category=" + category + ", name=" + name + ", url=" + url + ", id="
					+ id + "]";
		}
	}

	// Selects all fandoms without prejudice.
	private static final String UNBOUNDED_QUERY = "SELECT"
			+ " `fandom`.`id` AS `id`,"
			+ " `category`.`name` AS `category`,"
			+ " `fandom`.`name` AS `name`,"
			+ " `fandom`.`url` AS `url`"
			+ " FROM `fandom`"
			+ " INNER JOIN `category` ON `category`.`id` = `fandom`.`category_id`"
			+ " WHERE `fandom`.`id` > 266";
	private static final String ID_NAME = "`fandom`.`id`";
	private static final int BUFFER_SIZE = 10000;

	public FandomProducer() throws InterruptedException {
		super(ID_NAME, Fandom.class, Boot.getDatabaseConnectionPool(),
				BUFFER_SIZE, Boot.getLogger());
	}

	@Override
	protected String getUnboundedQuery() {
		return UNBOUNDED_QUERY;
	}
}
