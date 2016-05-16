package me.fru1t.fanfiction.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.FandomStoryListElement;
import me.fru1t.util.DatabaseUtils;

public class StoredProcedures {
	/**
	 * 1 category_name VARCHAR(128),
	 * 2 fandom_name VARCHAR(128),
	 * 3 user_ff_id INT,
	 * 4 user_name VARCHAR(128),
	 * 5 rating_name VARCHAR(45),
	 * 6 language_name VARCHAR(128),
	 * 7 ff_story_id INT,
	 * 8 title VARCHAR(256),
	 * 9 chapters INT,
	 * 10 words INT,
	 * 11 reviews INT,
	 * 12 favorites INT,
	 * 13 followers INT,
	 * 14 date_published INT(10),
	 * 15 date_updated INT(10),
	 * 16 is_complete TINYINT,
	 * 17 scrape_id INT,
	 * 18 session_name VARCHAR(128),
	 * 19 process_date INT(10),
	 * 20 meta_did_successfully_parse TINYINT,
	 * 21 story_did_successfully_parse TINYINT,
	 * 22 metadata TEXT
	 */
	private static final String USP_PROCESS_LIST_SCRAPE_TO_STORY =
			"{CALL usp_process_list_scrape_to_story(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
	
	/**
	 * 1 ff_story_id INT
	 * 2 character_name VARCHAR(128)
	 */
	private static final String USP_ADD_CHARACTER_TO_STORY =
			"{CALL usp_add_character_to_story(?, ?)}";

	/**
	 * 1 ff_story_id INT
	 * 2 genre_name VARCHAR(128)
	 */
	private static final String USP_ADD_GENRE_TO_STORY =
			"{CALL usp_add_genre_to_story(?, ?)}";
	
	/**
	 * 1 session_name VARCHAR(128)
	 * 2 scrape_date INT(10)
	 * 3 url VARCHAR(255)
	 * 4 content MEDIUMTEXT
	 */
	private static final String USP_ADD_SCRAPE =
			"{CALL usp_add_scrape(?,?,?,?)}";
	
	/**
	 * Adds scrape content to the database.
	 * @throws InterruptedException 
	 */
	public static void addScrape(String sessionName, String url, @Nullable String content) throws InterruptedException {
		int currentTime = (int) ((new Date()).getTime() / 1000);
		Connection c = Database.getConnection();
		if (content == null) {
			Boot.getLogger().log("Ignored raw scrape insert as the content was null");
			return;
		}
		
		try {
			CallableStatement stmt = c.prepareCall(USP_ADD_SCRAPE);
			stmt.setString(1, sessionName); // 1 session_name VARCHAR(128)
			stmt.setInt(2, currentTime); // 2 scrape_date INT(10)
			stmt.setString(3, url); // 3 url VARCHAR(255)
			stmt.setString(4, content); // 4 content MEDIUMTEXT
			DatabaseUtils.executeStatement(stmt, Boot.getLogger());
		} catch (SQLException e) {
			Boot.getLogger().log(e, "The following error occured when adding a scrape: ");
		}
	}
	
	/**
	 * Batch inserts the result of the processing of list scrapes.
	 */
	public static void processListScrapeToStory(
			int scrapeId,
			String sessionName,
			String categoryName,
			List<FandomStoryListElement> bookResultElements) {
		int dateProcessed = Math.round((new Date()).getTime()/1000f);
		Connection c = Database.getConnection();

		CallableStatement storyStmt = null;
		CallableStatement charStmt = null;
		CallableStatement genreStmt = null;
		try {
			// Prepare all statements
			storyStmt = c.prepareCall(USP_PROCESS_LIST_SCRAPE_TO_STORY);
			charStmt = c.prepareCall(USP_ADD_CHARACTER_TO_STORY);
			genreStmt = c.prepareCall(USP_ADD_GENRE_TO_STORY);
			
			for (FandomStoryListElement bre : bookResultElements) {
				// Element
				storyStmt.setString(1, categoryName); // 1 category_name VARCHAR(128),
				storyStmt.setString(2, bre.fandom); // 2 fandom_name VARCHAR(128),
				storyStmt.setInt(3, bre.ffUserId); // 3 user_ff_id INT,
				storyStmt.setString(4, bre.user_name); // 4 user_name VARCHAR(128),
				storyStmt.setString(5, bre.processedMetadata.rating); // 5 rating_name VARCHAR(45),
				storyStmt.setString(6, bre.processedMetadata.language); // 6 language_name VARCHAR(128),
				storyStmt.setInt(7, bre.ffBookId); // 7 ff_story_id INT,
				storyStmt.setString(8, bre.bookTitle); // 8 title VARCHAR(256),
				storyStmt.setInt(9, bre.processedMetadata.chapters); // 9 chapters INT,
				storyStmt.setInt(10, bre.processedMetadata.words); // 10 words INT,
				storyStmt.setInt(11, bre.processedMetadata.reviews); // 11 reviews INT,
				storyStmt.setInt(12, bre.processedMetadata.favorites); // 12 favorites INT,
				storyStmt.setInt(13, bre.processedMetadata.followers); // 13 followers INT,
				storyStmt.setInt(14, bre.processedMetadata.datePublished); // 14 date_published INT(10),
				storyStmt.setInt(15, bre.processedMetadata.dateUpdated);// 15 date_updated INT(10),
				storyStmt.setBoolean(16, bre.processedMetadata.isComplete); // 16 is_complete TINYINT,
				storyStmt.setInt(17, scrapeId); // 17 scrape_id INT,
				storyStmt.setString(18, sessionName); // 18 session_name VARCHAR(128),
				storyStmt.setInt(19, dateProcessed); // 19 process_date INT(10),
				storyStmt.setBoolean(20, bre.processedMetadata.didSuccessfullyParse); // 20 meta_did_successfully_parse TINYINT,
				storyStmt.setBoolean(21, bre.didSuccessfullyParse); // 21 story_did_successfully_parse TINYINT,
				storyStmt.setString(22, bre.metadata); // 22 metadata TEXT
				storyStmt.addBatch();
				
				// Characters
				for (String character : bre.processedMetadata.characters) {
					charStmt.setInt(1, bre.ffBookId); // 1 IN ff_book_id INT(11)
					charStmt.setString(2, character); // 2 character_name
					charStmt.addBatch();
				}
				
				// Genres
				for (String genre : bre.processedMetadata.genres) {
					genreStmt.setInt(1, bre.ffBookId); // 1 ff_book_id INT(11)
					genreStmt.setString(2, genre); // 2 genre_name VARCHAR(128)
					genreStmt.addBatch();
				}
			}
			storyStmt.executeBatch();
			charStmt.executeBatch();
			genreStmt.executeBatch();
		} catch (SQLException e) {
			Boot.getLogger().log(e);
		} finally {
			DatabaseUtils.closeStatement(storyStmt, Boot.getLogger());
			DatabaseUtils.closeStatement(charStmt, Boot.getLogger());
			DatabaseUtils.closeStatement(genreStmt, Boot.getLogger());
		}
	}
}
