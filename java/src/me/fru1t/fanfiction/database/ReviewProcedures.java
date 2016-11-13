package me.fru1t.fanfiction.database;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.ReviewListPage.ReviewElement;
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
	private static final String QUERY_INSERT_REVIEW_RELAX = 
			"INSERT INTO `review_relax` ()"  
			+ " VALUES ()";
	
	public static void processAddReviewBatch(String url,
			int ffStoryId, int chapter, List<ReviewElement> reviewElements) throws InterruptedException {

		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				PreparedStatement stmt = c.prepareStatement(QUERY_INSERT_REVIEW_RELAX);
				try {
					c.setAutoCommit(false);  
					//c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); 

					for (ReviewElement review : reviewElements) {
						stmt.setInt(1, review.reviewer_ff_id); // 1 `user_ff_id` INT(11) NULL,
						stmt.setString(2, review.reviewer_name); // 2 `user_name` varchar(128) NOT NULL,
						stmt.setInt(3, review.review_date); //   3 `date` INT(10) NULL,
						stmt.setInt(4, ffStoryId); //   4 `ff_story_id` INT NOT NULL,
						stmt.setInt(5, chapter); //   5 `chapter` INT NOT NULL,
						stmt.setString(6, review.content); //   6 `content` MEDIUMTEXT NULL,
						stmt.addBatch();
					}

					stmt.executeBatch();
					c.commit();

				} catch ( SQLException se ){
					String addReviewBatchfilename = "reviewUrl" + (new Date()).getTime() + ".txt";

					Boot.getLogger().log(se, "AddReviewBatch is having trouble! " 
							+ "Outputting a list of review urls that were supposed to be inserted to a file \""
							+ addReviewBatchfilename);

					try {
						BufferedWriter fileWriter = 
								new BufferedWriter(new FileWriter(addReviewBatchfilename, true));
						fileWriter.write(url + "\r\n");
						fileWriter.flush();
						fileWriter.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						Boot.getLogger().log(e1, "Couldn't write to file \"" + addReviewBatchfilename);
					}
				} finally {
					stmt.close();
				}
			}
		});
	}
	
}