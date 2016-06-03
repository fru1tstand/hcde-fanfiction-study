package me.fru1t.fanfiction.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.Session;
import me.fru1t.fanfiction.web.page.element.FandomStoryListElement;
import me.fru1t.util.DatabaseConnectionPool.Statement;

public class StoredProcedures {
	/**
	 * 1 category_name VARCHAR(128),
	 * 2 fandom_name VARCHAR(128),
	 * 3 in_fandom_url VARCHAR(1000),
	 * 4 user_ff_id INT,
	 * 5 user_name VARCHAR(128),
	 * 6 rating_name VARCHAR(45),
	 * 7 language_name VARCHAR(128),
	 * 8 ff_story_id INT,
	 * 9 title VARCHAR(256),
	 * 10 chapters INT,
	 * 11 words INT,
	 * 12 reviews INT,
	 * 13 favorites INT,
	 * 14 followers INT,
	 * 15 date_published INT(10),
	 * 16 date_updated INT(10),
	 * 17 is_complete TINYINT,
	 * 18 scrape_id INT,
	 * 19 session_name VARCHAR(128),
	 * 20 process_date INT(10),
	 * 21 meta_did_successfully_parse TINYINT,
	 * 22 story_did_successfully_parse TINYINT,
	 * 23 metadata TEXT
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
	public static void addScrape(Session session, String url, @Nullable String content)
			throws InterruptedException {
		int currentTime = (int) ((new Date()).getTime() / 1000);
		if (content == null) {
			Boot.getLogger().log("Ignored raw scrape insert as the content was null");
			return;
		}

		if (Boot.DEBUG) {
			Boot.getLogger().debug("Fake storing content length: "
					+ content.length() + "; url: " + url, StoredProcedures.class);
			return;
		}

		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				CallableStatement stmt = c.prepareCall(USP_ADD_SCRAPE);
				try {
					stmt.setString(1, session.name()); // 1 session_name VARCHAR(128)
					stmt.setInt(2, currentTime); // 2 scrape_date INT(10)
					stmt.setString(3, url); // 3 url VARCHAR(255)
					stmt.setString(4, content); // 4 content MEDIUMTEXT
					stmt.execute();
				} finally {
					stmt.close();
				}

			}
		});
	}

	/**
	 * Batch inserts the result of the processing of list scrapes.
	 * @throws InterruptedException
	 */
	public static void processListScrapeToStory(
			int scrapeId,
			String sessionName,
			String categoryName,
			String fandomUrl,
			List<FandomStoryListElement> bookResultElements) throws InterruptedException {
		int dateProcessed = Math.round((new Date()).getTime()/1000f);

		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				CallableStatement storyStmt = c.prepareCall(USP_PROCESS_LIST_SCRAPE_TO_STORY);
				CallableStatement charStmt = c.prepareCall(USP_ADD_CHARACTER_TO_STORY);
				CallableStatement genreStmt = c.prepareCall(USP_ADD_GENRE_TO_STORY);
				try {
					for (FandomStoryListElement bre : bookResultElements) {
						// Element
						storyStmt.setString(1, categoryName.trim()); // 1 category_name VARCHAR(128),
						storyStmt.setString(2, bre.fandom.trim()); // 2 fandom_name VARCHAR(128),
						storyStmt.setString(3, fandomUrl); // 3 in_fandom_url VARCHAR(1000),
						storyStmt.setInt(4, bre.ffUserId); // 4 user_ff_id INT,
						storyStmt.setString(5, bre.user_name.trim()); // 5 user_name VARCHAR(128),
						storyStmt.setString(6, bre.processedMetadata.rating.trim()); // 6 rating_name VARCHAR(45),
						storyStmt.setString(7, bre.processedMetadata.language.trim()); // 7 language_name VARCHAR(128),
						storyStmt.setInt(8, bre.ffBookId); //87 ff_story_id INT,
						storyStmt.setString(9, bre.bookTitle.trim()); // 9 title VARCHAR(256),
						storyStmt.setInt(10, bre.processedMetadata.chapters); // 10 chapters INT,
						storyStmt.setInt(11, bre.processedMetadata.words); // 11 words INT,
						storyStmt.setInt(12, bre.processedMetadata.reviews); // 12 reviews INT,
						storyStmt.setInt(13, bre.processedMetadata.favorites); // 13 favorites INT,
						storyStmt.setInt(14, bre.processedMetadata.followers); // 14 followers INT,
						storyStmt.setInt(15, bre.processedMetadata.datePublished); // 15 date_published INT(10),
						storyStmt.setInt(16, bre.processedMetadata.dateUpdated);// 16 date_updated INT(10),
						storyStmt.setBoolean(17, bre.processedMetadata.isComplete); // 17 is_complete TINYINT,
						storyStmt.setInt(18, scrapeId); // 18 scrape_id INT,
						storyStmt.setString(19, sessionName); // 19 session_name VARCHAR(128),
						storyStmt.setInt(20, dateProcessed); // 20 process_date INT(10),
						storyStmt.setBoolean(21, bre.processedMetadata.didSuccessfullyParse); // 21 meta_did_successfully_parse TINYINT,
						storyStmt.setBoolean(22, bre.didSuccessfullyParse); // 22 story_did_successfully_parse TINYINT,
						storyStmt.setString(23, bre.metadata); // 23 metadata TEXT
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
				} finally {
					storyStmt.close();
					charStmt.close();
					genreStmt.close();
				}
			}
		});
	}
}
