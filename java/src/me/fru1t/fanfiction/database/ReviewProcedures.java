package me.fru1t.fanfiction.database;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.ReviewElement;
import me.fru1t.util.DatabaseConnectionPool.Statement;

public class ReviewProcedures {
	
	
	/**
     * 1. in_user_ff_id INT,
     * 2. in_user_name VARCHAR(512),
     * 3. in_story_id INT,
     * 4. in_date INT(10),
     * 5. in_chapter INT,
     * 6. in_content MEDIUMTEXT
	 */
	private static final String USP_ADD_REVIEW_REVIEWER =
			"{CALL usp_add_review_reviewer (?,?,?, ?,?,?)}";
	
	public static void addReviewReviewer(List<ReviewElement> reviewElements) throws InterruptedException {

		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {

				PreparedStatement storyStmt = c.prepareStatement("");
				CallableStatement reviewStmt = c.prepareCall(USP_ADD_REVIEW_REVIEWER);
				try {
					c.setAutoCommit(false);  
					
				    // GET STORY IDs
					HashSet<String> ffStoryIds = new HashSet<>();
					for (ReviewElement relem : reviewElements)
						ffStoryIds.add(Integer.toString(relem.ff_story_id));
					
					String selectStoryQuery = String.format(
							"SELECT ff_story_id AS ffStoryId, id AS storyId FROM `story` WHERE ff_story_id in (%s)", 
							String.join(",", ffStoryIds));
					
					ResultSet rs = storyStmt.executeQuery(selectStoryQuery);
					HashMap<Integer, Integer> ffStoryId_to_storyId = new HashMap<>();
				    while (rs.next()) ffStoryId_to_storyId.put(rs.getInt("ffStoryId"), rs.getInt("storyId"));
				    
				    // INSERT REVIEW and REVIEWER
					for (ReviewElement relem : reviewElements) {
						if (relem.reviewer_ff_id > 0) 
							reviewStmt.setInt(1, relem.reviewer_ff_id); // 1 `user_ff_id` INT(11) NULL,
						else 
							reviewStmt.setNull(1, Types.INTEGER);
						
						reviewStmt.setString(2, relem.reviewer_name); // 2 user_name
						
						reviewStmt.setInt(3, ffStoryId_to_storyId.get(relem.ff_story_id)); //   3 `story_id` INT NOT NULL,
						
						reviewStmt.setInt(4, relem.review_date); //   3 `date` INT(10) DEFAULT -1
						
						reviewStmt.setInt(5, relem.chapter); //   5 `chapter` INT NOT NULL,
						
						reviewStmt.setString(6, relem.content); //   6 `content` MEDIUMTEXT NULL,
						
						reviewStmt.addBatch();
					}

					reviewStmt.executeBatch();
					c.commit();

				} catch ( SQLException se ){
					String addReviewBatchfilename = "reviewScrapeId" + (new Date()).getTime() + ".txt";

					Boot.getLogger().log(se, "AddReviewBatch is having trouble! " 
							+ "Outputting a list of review urls that were supposed to be inserted to a file \""
							+ addReviewBatchfilename);

					try {
						BufferedWriter fileWriter = 
								new BufferedWriter(new FileWriter(addReviewBatchfilename, true));
						for (ReviewElement relem : reviewElements) {
							fileWriter.write(relem.getScrapeId() + "\r\n");
							fileWriter.flush();
						}
						fileWriter.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						Boot.getLogger().log(e1, "Couldn't write to file \"" + addReviewBatchfilename);
					}
				} finally {
					storyStmt.close();
					reviewStmt.close();
				}
			}
		});
	}
	
	
	/************************************************************************************************/
	
	/**
     * 1. in_user_ff_id INT,
     * 2. in_user_name VARCHAR(512),
     * 3. in_ff_story_id INT,
     * 4. in_date INT(10),
     * 5. in_chapter INT,
     * 6. in_content MEDIUMTEXT
	 */
	private static final String INSERT_REVIEW_DIRECT =
			"INSERT INTO `review_direct` (user_ff_id, user_name, ff_story_id, date, chapter, content) "
			+ "VALUES (?,?,?,?,?,?)";
	
	public static void addReviewDirect(List<ReviewElement> reviewElements) throws InterruptedException {

		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {

				PreparedStatement reviewStmt = c.prepareStatement(INSERT_REVIEW_DIRECT);
				try {
					c.setAutoCommit(false);  
					
				    // INSERT REVIEW and REVIEWER
					for (ReviewElement relem : reviewElements) {
						if (relem.reviewer_ff_id > 0) 
							reviewStmt.setInt(1, relem.reviewer_ff_id); // 1 `user_ff_id` INT(11) NULL,
						else 
							reviewStmt.setNull(1, Types.INTEGER);
						
						reviewStmt.setString(2, relem.reviewer_name); // 2 user_name
						
						reviewStmt.setInt(3, relem.ff_story_id); //   3 `story_id` INT NOT NULL,
						
						reviewStmt.setInt(4, relem.review_date); //   3 `date` INT(10) DEFAULT -1
						
						reviewStmt.setInt(5, relem.chapter); //   5 `chapter` INT NOT NULL,
						
						reviewStmt.setString(6, relem.content); //   6 `content` MEDIUMTEXT NULL,
						
						reviewStmt.addBatch();
					}

					reviewStmt.executeBatch();
					c.commit();

				} catch ( SQLException se ){
					String addReviewBatchfilename = "reviewScrapeId" + (new Date()).getTime() + ".txt";

					Boot.getLogger().log(se, "AddReviewBatch is having trouble! " 
							+ "Outputting a list of review urls that were supposed to be inserted to a file \""
							+ addReviewBatchfilename);

					try {
						BufferedWriter fileWriter = 
								new BufferedWriter(new FileWriter(addReviewBatchfilename, true));
						for (ReviewElement relem : reviewElements) {
							fileWriter.write(relem.getScrapeId() + "\r\n");
							fileWriter.flush();
						}
						fileWriter.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						Boot.getLogger().log(e1, "Couldn't write to file \"" + addReviewBatchfilename);
					}
				} finally {
					reviewStmt.close();
				}
			}
		});
	}
}