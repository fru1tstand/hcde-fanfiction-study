package me.fru1t.fanfiction.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.StoryListElement;
import me.fru1t.util.DatabaseConnectionPool.Statement;

public class StoryListProcedures {
	/**
		in_fandom_url VARCHAR(2000),
	    in_user_ff_id INT,
	    in_user_name VARCHAR(128),
	    in_rating_name VARCHAR(45),
	    in_language_name VARCHAR(128),
	
	    -- story data 
	    in_ff_story_id INT,
	    in_title VARCHAR(256),
	    
	    in_chapters INT,
	    in_words INT,
	    
	    in_reviews INT,
	    in_favorites INT,
	    
	    in_followers INT,
	    in_date_published INT(10),
	    
	    in_date_updated INT(10),
	    in_is_complete TINYINT
	 */
	private static final String USP_ADD_STORY_VALUES =
			"{CALL usp_add_story_values (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}";

	/**
	 * 1 ff_story_id INT
	 * 2 character_name VARCHAR(128)
	 */
	private static final String USP_ADD_CHARACTER_TO_STORY = "{CALL usp_add_character_to_story(?, ?)}";

	/**
	 * 1 ff_story_id INT
	 * 2 genre_name VARCHAR(128)
	 */
	private static final String USP_ADD_GENRE_TO_STORY = "{CALL usp_add_genre_to_story(?, ?)}";
	
	public static void addStoryValues(
			String scrape_url,
			String fandomUrl_inFandom,
			ArrayList<StoryListElement> storyListElements) throws InterruptedException {

		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				c.setAutoCommit(false); 
				
				ArrayList<String> ffBookIds = new ArrayList<>();
				CallableStatement storyStmt = c.prepareCall(USP_ADD_STORY_VALUES);
				CallableStatement charStmt = c.prepareCall(USP_ADD_CHARACTER_TO_STORY);
				CallableStatement genreStmt = c.prepareCall(USP_ADD_GENRE_TO_STORY);
				
				try { 
					for (StoryListElement sElem : storyListElements) {
						// Sanity Check
						if (sElem.ffBookId < 0 || sElem.bookTitle == null) {
							Boot.getLogger().log("[Invalid story] Skipping a story on url : " + scrape_url, true);
							continue;
						}
						
						if (sElem.ffUserId < 0 || sElem.user_name == null) {
							Boot.getLogger().log("[Invalid user] Skipping a story with ff_story_id " 
									+ sElem.ffBookId + " on url : " + scrape_url, true);
							continue;
						}
						
						if (sElem.processedMetadata == null) {
							Boot.getLogger().log("[Invalid meta] Skipping a story with ff_story_id " 
									+ sElem.ffBookId + " on url : " + scrape_url, true);
							continue;
						}
						
						if (sElem.processedMetadata.rating == null) {
							Boot.getLogger().log("[Invalid rating] Skipping a story with ff_story_id " 
									+ sElem.ffBookId + " on url : " + scrape_url, true);
							continue;
						}
						
						if (sElem.processedMetadata.language == null) {
							Boot.getLogger().log("[Invalid lanugage] Skipping a story with ff_story_id " 
									+ sElem.ffBookId + " on url : " + scrape_url, true);
							continue;
						}
						
						ffBookIds.add(Integer.toString(sElem.ffBookId));
						
						// foreign keys
						storyStmt.setString(1, fandomUrl_inFandom); // in_fandom_url VARCHAR(2000), e.g. /anime/Hetalia-Axis-Powers/
						storyStmt.setInt(2, sElem.ffUserId); // in_user_ff_id INT,
					    storyStmt.setString(3, sElem.user_name.trim()); // in_user_name VARCHAR(128),
					    storyStmt.setString(4, sElem.processedMetadata.rating.trim()); // in_rating_name VARCHAR(45),
					    storyStmt.setString(5, sElem.processedMetadata.language.trim()); // in_language_name VARCHAR(128),
					
					    // story data 
					    storyStmt.setInt(6, sElem.ffBookId); // in_ff_story_id INT,
					    storyStmt.setString(7, sElem.bookTitle.trim()); // in_title VARCHAR(256),
					    
					    storyStmt.setInt(8, sElem.processedMetadata.chapters); // in_chapters INT,
					    storyStmt.setInt(9, sElem.processedMetadata.words); // in_words INT,
					    
					    storyStmt.setInt(10, sElem.processedMetadata.reviews); // in_reviews INT,
					    storyStmt.setInt(11, sElem.processedMetadata.favorites); // in_favorites INT,
					    
					    storyStmt.setInt(12, sElem.processedMetadata.followers); // in_followers INT,
					    storyStmt.setInt(13, sElem.processedMetadata.datePublished); // in_date_published INT(10),
					    
					    storyStmt.setInt(14, sElem.processedMetadata.dateUpdated); // in_date_updated INT(10),
					    storyStmt.setBoolean(15, sElem.processedMetadata.isComplete); // in_is_complete TINYINT,
					    
					    // storyStmt.registerOutParameter(16, java.sql.Types.INTEGER); // OUT out_v_story_id INT
					    // int story_id = storyStmt.getInt(16); // not ff_story_id
					
					    storyStmt.addBatch();
					}
					
					storyStmt.executeBatch();
					
					String selectQuery = String.format(
							"SELECT ff_story_id AS ffStoryId, id AS storyId FROM `story` WHERE ff_story_id in (%s)", 
							String.join(",", ffBookIds));
					
					ResultSet rs = storyStmt.executeQuery(selectQuery);
					HashMap<Integer, Integer> ffStoryId_to_storyId = new HashMap<>();
				    while (rs.next()) {
				    	ffStoryId_to_storyId.put(rs.getInt("ffStoryId"), rs.getInt("storyId"));
				    }
					
					for (StoryListElement sElem : storyListElements) {
					    // Characters
					    for (String character : sElem.processedMetadata.characters) {
					    	charStmt.setInt(1, ffStoryId_to_storyId.get(sElem.ffBookId)); 
					    	// bre.ffBookId); // 1 IN ff_book_id INT(11)
					    	charStmt.setString(2, character); // 2 character_name
					    	charStmt.addBatch();
					    }
					    
					    // Genres
					    for (String genre : sElem.processedMetadata.genres) {
					    	genreStmt.setInt(1, ffStoryId_to_storyId.get(sElem.ffBookId)); 
					    	// bre.ffBookId); // 1 IN ff_book_id INT(11)
					    	genreStmt.setString(2, genre); // 2 genre_name VARCHAR(128)
					    	genreStmt.addBatch();
					    }
					}
					
					charStmt.executeBatch();
					genreStmt.executeBatch();
					
					c.commit();
				} finally {
					storyStmt.close();
					charStmt.close();
					genreStmt.close();
				}
			}
		});
	}
}
