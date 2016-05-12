package me.fru1t.fanfiction.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.Database;
import me.fru1t.fanfiction.database.producers.MetadataFixScrapeProducer;
import me.fru1t.fanfiction.database.producers.MetadataFixScrapeProducer.MetadataScrape;

public class FixMetadataProcess implements Runnable {
	private static final String UPDATE_QUERY =
			"UPDATE `scrape_book_result_element` SET `meta_chapters` = ? WHERE `id` = ?";
	private static final int DATABASE_COMMIT_FREQUENCY = 100;
	private static final Pattern CHAPTERS_EXTRACTION_PATTERN =
			Pattern.compile("^.+ Chapters: (\\d+) .+$");

	@Override
	public void run() {
		MetadataFixScrapeProducer mfsp = new MetadataFixScrapeProducer();
		List<MetadataScrape> fixedMetadatas = null;
		MetadataScrape brokenScrape = mfsp.take();
		while (brokenScrape != null) {
			if (fixedMetadatas == null) {
				fixedMetadatas = new ArrayList<>();
			}
			
			Matcher m = CHAPTERS_EXTRACTION_PATTERN.matcher(brokenScrape.metadata);
			if (m.matches()) {
				try {
					brokenScrape.metaChapters = Integer.parseInt(m.group(1));
					fixedMetadatas.add(brokenScrape);
				} catch (NumberFormatException e) {
					Boot.getLogger().log(e);
				}
			} else {
				Boot.getLogger().log("Couldn't find chapters for the string: "
						+ brokenScrape.metadata);
			}
			
			if (fixedMetadatas.size() >= DATABASE_COMMIT_FREQUENCY) {
				batchUpdateChapters(fixedMetadatas);
				fixedMetadatas = null;
			}
			
			brokenScrape = mfsp.take();
		}
		
		// Flush remaining changes to database
		if (fixedMetadatas != null) {
			batchUpdateChapters(fixedMetadatas);
		}
	}
	

	private void batchUpdateChapters(List<MetadataScrape> metadatas) {
		Connection c = Database.getConnection();
		try {
			PreparedStatement stmt = c.prepareStatement(UPDATE_QUERY);
			for (MetadataScrape metadata : metadatas) {
				if (metadata.metaChapters == -1) {
					continue;
				}
				stmt.setInt(1, metadata.metaChapters);
				stmt.setInt(2, metadata.id);
				stmt.addBatch();
			}
			stmt.executeBatch();
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
			
	}
}
