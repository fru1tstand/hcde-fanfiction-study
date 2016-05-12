package me.fru1t.fanfiction.database.producers;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.Database;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Fix before use.
 * @deprecated 
 */
public class MetadataFixScrapeProducer extends DatabaseProducer<MetadataFixScrapeProducer.MetadataScrape, Integer> {
	public static class MetadataScrape extends DatabaseProducer.Row<Integer> {
		public static final String COLUMN_METADATA = "metadata";
		public static final String COLUMN_META_CHAPTERS = "metaChapters";
		
		public String metadata;
		public int metaChapters;
	}
	
	private static final String ID_NAME = "`scrape_book_result_element`.`id`";
	
	public MetadataFixScrapeProducer() {
		super(ID_NAME, MetadataScrape.class, Database.getConnection(), 300, Boot.getLogger());
	}

	@Override
	protected String getUnboundedQuery() {
		return "SELECT `id` AS `id`, `metadata` AS `metadata`, `meta_chapters` AS `metaChapters`"
				+ " FROM `scrape_book_result_element`"
				+ " WHERE `meta_chapters` = -1";
	}
}
