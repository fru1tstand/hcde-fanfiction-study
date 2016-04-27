package me.fru1t.fanfiction.database.schema.scrape;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.Database;
import me.fru1t.util.concurrent.DatabaseProducer;

public class BookIdAndChaptersProducer extends DatabaseProducer<BookIdAndChaptersProducer.BookIdAndChapters, Integer> {
	public static class BookIdAndChapters extends DatabaseProducer.Row<Integer> {
		public static final String COLUMN_BOOK_ID = "bookId";
		public static final String COLUMN_META_CHAPTERS = "metaChapters";
		public int bookId;
		public int metaChapters;
	}
	
	private static final String ID_NAME = "`scrape_book_result_element`.`id`";
	// At least 10 reviews
	// At least 2 reviews per chapter
	// Author at least 5 years old
	// Author has at least 10 stories published
	private static final String RESTRICTED_QUERY = "SELECT"
			+ "`id` AS `id`,"
			+ "`ff_book_id` AS `bookId`,"
			+ "`meta_chapters` AS `metaChapters`"
			+ "FROM `fanfiction`.`scrape_book_result_element`"
			+ "WHERE `ff_author_id` IN (SELECT `ff_author_id`"
				+ "FROM `scrape_book_result_element`"
				+ "GROUP BY `ff_author_id` HAVING COUNT(`id`) > 10"
				+ "AND MIN(`meta_date_published`) > UNIX_TIMESTAMP(DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -5 YEAR)))"
			+ "AND `meta_reviews` >= 10 "
			+ "AND `meta_reviews`/`meta_chapters` >= 2";
	
	public BookIdAndChaptersProducer() {
		super(ID_NAME, BookIdAndChapters.class, Database.getConnection(), 300, Boot.getLogger());
	}
	
	@Override
	protected String getUnboundedQuery() {
		return RESTRICTED_QUERY;
	}

}
