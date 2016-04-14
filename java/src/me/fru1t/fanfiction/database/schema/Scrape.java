package me.fru1t.fanfiction.database.schema;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.Database;
import me.fru1t.fanfiction.web.page.element.BookResultElement;
import me.fru1t.util.concurrent.DatabaseProducer;

public class Scrape {
	public static final String BROWSE_BY_BOOK_TYPE = "browse-by-book";
	
	/**
	 * Represents the scrape_raw table in the fanfiction database.
	 */
	public static class ScrapeRaw extends DatabaseProducer.Row<Integer> {
		public static final String COLUMN_ID = "id";
		public static final String COLUMN_SCRAPE_SESSION_ID = "scrapeSessionId";
		public static final String COLUMN_DATE = "date";
		public static final String COLUMN_URL = "url";
		public static final String COLUMN_CONTENT = "content";
		public static final String COLUMN_SCRAPE_TYPE_ID = "scrapeTypeId";
		
		/** scrape_session_id INT(11) */
		public int scrapeSessionId;
		
		/** date INT(10) */
		public int date;
		
		/** url VARCHAR(255) */
		public String url;
		
		/** content MEDIUMTEXT*/
		public String content;
		
		/** scrape_type_id INT(11) */
		public int scrapeTypeId;
	}
	
	/**
	 * 1 IN scrape_session_name VARCHAR(128),
	 * 2 IN scrape_type_name VARCHAR(128)
	 * 3 IN scrape_date INT
	 * 4 IN scrape_url VARCHAR(255)
	 * 5 IN scrape_content MEDIUMTEXT
	 */
	private static final String USP_SCRAPE_ADD_RAW =
			"{CALL usp_scrape_add_raw(?, ?, ?, ?, ?)}";
	
	/**
	 * 1  scrape_raw_id INT
	 * 2  scrape_process_session_name VARCHAR(128)
	 * 3  ff_real_book_name VARCHAR(128)
	 * 4  date_processed int(11)
	 * 5  ff_book_id int(11)
	 * 6  ff_author_id int(11)
	 * 7  book_title varchar(256)
	 * 8  book_url varchar(1024)
	 * 9  cover_image_url varchar(1024)
	 * 10  cover_image_original_url varchar(1024)
	 * 11 author varchar(64)
	 * 12 author_url varchar(1024)
	 * 13 synopsis varchar(2048)
	 * 14 metadata varchar(1024)
	 * 15 meta_rating varchar(2)
	 * 16 meta_language varchar(45)
	 * 17 meta_chapters int(11)
	 * 18 meta_words int(11)
	 * 19 meta_reviews int(11)
	 * 20 meta_favorites int(11)
	 * 21 meta_followers int(11)
	 * 22 meta_date_updated int(11)
	 * 23 meta_date_published int(11)
	 * 24 meta_genres varchar(512)
	 * 25 meta_characters varchar(512)
	 * 26 meta_is_complete tinyint(4)
	 * 27 meta_did_successfully_parse tinyint(4)
	 * 28 did_successfully_parse tinyint(4)
	 */
	private static final String USP_SCRAPE_ADD_PROCESSED_BOOK_RESULT_ELEMENT =
			"{CALL usp_scrape_add_processed_book_result_element"
			+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
	
	/**
	 * 1 IN real_book_name VARCHAR(128),
	 * 2 IN character_name VARCHAR(128),
	 * 3 IN ff_book_id INT(11)
	 */
	private static final String USP_SCRAPE_ADD_PROCESSED_BOOK_RESULT_CHARACTER =
			"{CALL usp_scrape_add_processed_book_result_character(?, ?, ?)}";
	
	/**
	 * 1 genre_name VARCHAR(128)
	 * 2 ff_book_id INT(11)
	 */
	private static final String USP_SCRAPE_ADD_PROCESSED_BOOK_RESULT_GENRE =
			"{CALL usp_scrape_add_processed_book_result_genre(?, ?)}";
	
	public static void insertRaw(
			String sessionName, String scrapeType, String url, @Nullable String content) {
		int currentTime = (int) ((new Date()).getTime() / 1000);
		
		Connection c = Database.getConnection();
		if (c == null) {
			Boot.getLogger().log("Ignored raw scrape insert as the database couldn't be reached");
			return;
		}
		if (content == null) {
			Boot.getLogger().log("Ignored raw scrape insert as the content was null");
			return;
		}
		
		try {
			CallableStatement stmt = c.prepareCall(USP_SCRAPE_ADD_RAW);
			stmt.setString(1, sessionName); // 1 IN scrape_session_name VARCHAR(128),
			stmt.setString(2, scrapeType); // 2 IN scrape_type_name VARCHAR(128)
			stmt.setInt(3, currentTime); // 3 IN scrape_date INT
			stmt.setString(4, url); // 4 IN scrape_url VARCHAR(255)
			stmt.setString(5, content); // 5 IN scrape_content MEDIUMTEXT
			stmt.execute();
			stmt.close();
		} catch (SQLException e) {
			Boot.getLogger().log(e, null);
		}
	}
	
	public static void uspScrapeAddProcessedBookResultElement(
			int scrapeRawId,
			String processSessionName,
			List<BookResultElement> bookResultElements) {
		// Unix time in seconds
		int dateProcessed = Math.round((new Date()).getTime()/1000f);
		
		try {
			Connection c = Database.getConnection();
			if (c == null) {
				Boot.getLogger()
						.log("Couldn't reach database when adding processed book result elements");
				return;
			}
			
			// Prepare calls to all statement types
			CallableStatement elStmt =
					c.prepareCall(USP_SCRAPE_ADD_PROCESSED_BOOK_RESULT_ELEMENT);
			CallableStatement charStmt =
					c.prepareCall(USP_SCRAPE_ADD_PROCESSED_BOOK_RESULT_CHARACTER);
			CallableStatement genreStmt =
					c.prepareCall(USP_SCRAPE_ADD_PROCESSED_BOOK_RESULT_GENRE);
			
			for (BookResultElement bre : bookResultElements) {
				// Element
				elStmt.setInt(1, scrapeRawId); // 1  scrape_raw_id INT
				elStmt.setString(2, processSessionName); // 2  scrape_process_session_name VARCHAR(128)
				elStmt.setString(3, bre.realBookName); // 3  ff_real_book_name VARCHAR(128)
				elStmt.setInt(4, dateProcessed); // 4  date_processed int(11)
				elStmt.setInt(5, bre.ffBookId); // 5  ff_book_id int(11)
				elStmt.setInt(6, bre.ffAuthorId); // 6  ff_author_id int(11)
				elStmt.setString(7, bre.bookTitle); // 7  book_title varchar(256)
				elStmt.setString(8, bre.bookUrl); // 8  book_url varchar(1024)
				elStmt.setString(9, bre.coverImageUrl); // 9  cover_image_url varchar(1024)
				elStmt.setString(10, bre.coverImageOriginalUrl); // 10  cover_image_original_url varchar(1024)
				elStmt.setString(11, bre.author); // 11 author varchar(64)
				elStmt.setString(12, bre.authorUrl); // 12 author_url varchar(1024)
				elStmt.setString(13, bre.synopsis); // 13 synopsis varchar(2048)
				elStmt.setString(14, bre.metadata); // 14 metadata varchar(1024)
				elStmt.setString(15, bre.processedMetadata.rating); // 15 meta_rating varchar(2)
				elStmt.setString(16, bre.processedMetadata.language); // 16 meta_language varchar(45)
				elStmt.setInt(17, bre.processedMetadata.chapters); // 17 meta_chapters int(11)
				elStmt.setInt(18, bre.processedMetadata.words); // 18 meta_words int(11)
				elStmt.setInt(19, bre.processedMetadata.reviews); // 19 meta_reviews int(11)
				elStmt.setInt(20, bre.processedMetadata.favorites); // 20 meta_favorites int(11)
				elStmt.setInt(21, bre.processedMetadata.followers); // 21 meta_followers int(11)
				elStmt.setInt(22, bre.processedMetadata.dateUpdated); // 22 meta_date_updated int(11)
				elStmt.setInt(23, bre.processedMetadata.datePublished); // 23 meta_date_published int(11)
				elStmt.setString(24, String.join(",", bre.processedMetadata.genres)); // 24 meta_genres varchar(512)
				elStmt.setString(25, String.join(",", bre.processedMetadata.characters)); // 25 meta_characters varchar(512)
				elStmt.setInt(26, (bre.processedMetadata.isComplete) ? 1 : 0); // 26 meta_is_complete tinyint(4)
				elStmt.setInt(27, (bre.processedMetadata.didSuccessfullyParse) ? 1 : 0); // 27 meta_did_successfully_parse tinyint(4)
				elStmt.setInt(28, (bre.didSuccessfullyParse) ? 1 : 0); // 28 did_successfully_parse tinyint(4)
				elStmt.addBatch();
				
				// Characters
				for (String bookChar : bre.processedMetadata.characters) {
					charStmt.setString(1, bre.realBookName); // 1 IN real_book_name VARCHAR(128),
					charStmt.setString(2, bookChar); // 2 IN character_name VARCHAR(128),
					charStmt.setInt(3, bre.ffBookId); // 3 IN ff_book_id INT(11)
					charStmt.addBatch();
				}
				
				// Genres
				for (String genre : bre.processedMetadata.genres) {
					genreStmt.setString(1, genre); // 1 genre_name VARCHAR(128)
					genreStmt.setInt(2, bre.ffBookId); // 2 ff_book_id INT(11)
					genreStmt.addBatch();
				}
			}
			elStmt.executeBatch();
			elStmt.close();
			charStmt.executeBatch();
			charStmt.close();
			genreStmt.executeBatch();
			genreStmt.close();
		} catch (SQLException e) {
			Boot.getLogger().log(e);
		}
	}
}
