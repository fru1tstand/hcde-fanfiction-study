package me.fru1t.fanfiction.database;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.StoryContentElement;
import me.fru1t.util.DatabaseConnectionPool.Statement;

public class StoryContentProcedures {
	

	/**
	 *  1 `story_id` INT(11) NOT NULL,
	 *  2 `chapter` INT(11) NOT NULL
	 *  3 `chapter_title` 
	 *  4 `content`
	 */
	private static final String INSERT_QUERY_STORY_CONTENT = 
			"INSERT IGNORE INTO `story_content` " + 
			"(`story_id`, `chapter`, `chapter_title`, `content`) VALUES (?, ?, ?, ?)";


	public static void addStoryContents(ArrayList<StoryContentElement> StoryElements) 
			throws InterruptedException {
			Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				PreparedStatement selectStmt = c.prepareStatement("");
				PreparedStatement storyStmt = c.prepareStatement(INSERT_QUERY_STORY_CONTENT);

				try {
					c.setAutoCommit(false);
				    
				    // GET Story IDs
					ArrayList<String> ffStoryIds = new ArrayList<>();
					for (StoryContentElement se : StoryElements) { 
						ffStoryIds.add(Integer.toString(se.ff_story_id));
					}
					String selectStoryQuery = String.format(
							"SELECT ff_story_id AS ffStoryId, id AS storyId FROM `story` WHERE ff_story_id in (%s)", 
							String.join(",", ffStoryIds));
					ResultSet rs = selectStmt.executeQuery(selectStoryQuery);
					HashMap<Integer, Integer> ffStoryId_to_storyId = new HashMap<>();
					while (rs.next()) ffStoryId_to_storyId.put(rs.getInt("ffStoryId"), rs.getInt("storyId")); 
					
					
					// ADD to `story_content`
					for (StoryContentElement se : StoryElements) {
						storyStmt.setInt(1, ffStoryId_to_storyId.get(se.ff_story_id));
						storyStmt.setInt(2, se.chapter);
						storyStmt.setString(3, se.title);
						storyStmt.setString(4, se.content);
						storyStmt.addBatch();
					}

					storyStmt.executeBatch();
					
					c.commit();

				} catch (BatchUpdateException e) {
					String filename =  "userurl" + (new Date()).getTime() + ".txt";

					Boot.getLogger().log(e, "processUserScrapeToProfileBatch is having trouble! " 
							+ "Outputting a list of urls that were supposed to be inserted to a file \""
							+ filename);

					try {
						BufferedWriter fileWriter = 
								new BufferedWriter(new FileWriter(filename, true));
						for (StoryContentElement se : StoryElements) {
							fileWriter.write(se.getScrapeId() + "\r\n");
							fileWriter.flush();
						}
						fileWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						Boot.getLogger().log(e1, "Couldn't write to file \"" + filename);
					}
				} finally {
					selectStmt.close();
					storyStmt.close();
				}
			}
		});
	}
}