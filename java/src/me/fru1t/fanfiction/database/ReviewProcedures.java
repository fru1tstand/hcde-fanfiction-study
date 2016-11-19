package me.fru1t.fanfiction.database;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.ReviewElement;
import me.fru1t.util.DatabaseConnectionPool.Statement;

public class ReviewProcedures {
	/**
	 *   1 `user_ff_id` INT(11) NULL,
	 *   2 `user_name` varchar(128) NOT NULL,
	 *   3 `date` INT(10) NULL,
	 *   4 `ff_story_id` INT NOT NULL,
	 *   5 `chapter` INT NOT NULL,
	 *   6 `content` MEDIUMTEXT NULL,
	 */
	  
	  
	private static final String QUERY_INSERT_REVIEW = 
			"INSERT INTO `review` (user_ff_id, user_name, date, ff_story_id, chapter, content)"  
			+ " VALUES (?, ?, ?, ?, ?, ?)";
	
	private static final String QUERY_INSERT_USER = 
			"INSERT IGNORE INTO `user` (ff_id, user_name) VALUES (?, ?)";
	
	public static void addReviewAndReviewerBatch(List<ReviewElement> reviewElements) throws InterruptedException {

		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				PreparedStatement reviewStmt = c.prepareStatement(QUERY_INSERT_REVIEW);
				PreparedStatement userStmt = c.prepareStatement(QUERY_INSERT_USER);
				try {
					c.setAutoCommit(false);  
					//c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); 

					for (ReviewElement relem : reviewElements) {
						if (relem.reviewer_ff_id > 0) {
							reviewStmt.setInt(1, relem.reviewer_ff_id); // 1 `user_ff_id` INT(11) NULL,
							userStmt.setInt(1, relem.reviewer_ff_id);
							userStmt.setString(2, relem.reviewer_name);
						} else 
							reviewStmt.setNull(1, Types.INTEGER);
						
						reviewStmt.setString(2, relem.reviewer_name); // 2 `user_name` varchar(128) NOT NULL,
						
						if (relem.review_date > 0) 
							reviewStmt.setInt(3, relem.review_date); //   3 `date` INT(10) NULL,
						else
							reviewStmt.setNull(3, Types.INTEGER);
						
						reviewStmt.setInt(4, relem.ff_story_id); //   4 `ff_story_id` INT NOT NULL,
						reviewStmt.setInt(5, relem.chapter); //   5 `chapter` INT NOT NULL,
						reviewStmt.setString(6, relem.content); //   6 `content` MEDIUMTEXT NULL,
						reviewStmt.addBatch();
					}

					reviewStmt.executeBatch();
					userStmt.executeBatch();
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
					userStmt.close();
				}
			}
		});
	}
	
}