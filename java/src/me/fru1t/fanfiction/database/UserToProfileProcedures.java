package me.fru1t.fanfiction.database;

import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.ProfileElement;
import me.fru1t.fanfiction.web.page.element.ProfileElement.FavAuthor;
import me.fru1t.util.DatabaseConnectionPool.Statement;

public class UserToProfileProcedures {
	/**
	 * 1 in_ff_id int(11), not null
	 * 2 in_user_name varchar(128), not null
     * 3 in_country_name VARCHAR(128),
     * 4 in_join_date int(10),
     * 5 in_update_date int(10),
     * 6 in_bio mediumtext,
     * 7 in_age tinyint(4),
     * 8 in_gender char(6)
	 */
	private static final String USP_ADD_USER_PROFILE_RELAX =
			"{CALL usp_add_user_profile_relax(?,?,?,?,?,?,?,?)}";
		
	/**
	 *  1 `ff_id` INT(11) NOT NULL,
	 *  2 `favorite_ff_id` INT(11) NOT NULL,
	 *  3 `favorite_name` varchar(128) NOT NULL,
	 */
	private static final String INSERT_QUERY_USER_FAV_AUTHOR_RELAX = 
			"INSERT INTO `user_favorite_author_relex` (`ff_id`, `favorite_ff_id`, `favorite_name`)"  
			+ " VALUES (?, ?, ?)";
	
	/**
	 *  1 `ff_id` INT(11) NOT NULL,
	 *  2 `story_id` INT NOT NULL,
	 */
	private static final String INSERT_QUERY_USER_FAV_STORY_RELAX = 
			"INSERT INTO `user_favorite_story_relax` (`ff_id`, `story_id`)"  
			+ " VALUES (?, ?)";
	
	public static void processUserScrapeToProfile(
			ProfileElement pe
		) throws InterruptedException {
		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				CallableStatement profileStmt = c.prepareCall(USP_ADD_USER_PROFILE_RELAX);
				PreparedStatement favAuthorStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_AUTHOR_RELAX);
				PreparedStatement favStoryStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_STORY_RELAX);

				try {
					profileStmt.setInt(1, pe.my_ff_id); // in_ff_id
					profileStmt.setString(2, pe.user_name); // user_name
					profileStmt.setString(3, pe.country_name); // country_name
					profileStmt.setInt(4, pe.join_date); // join_date
					profileStmt.setInt(5, pe.update_date); // update_date
					profileStmt.setString(6, pe.bio); // bio
					profileStmt.setInt(7, pe.age); // age
					profileStmt.setString(8, pe.gender); // gender
					profileStmt.addBatch();

					for (FavAuthor fa : pe.myFavAuthors) {
						favAuthorStmt.setInt(1, pe.my_ff_id); // my_ff_id
						favAuthorStmt.setInt(2, fa.ff_id); // favorite_user_id
						favAuthorStmt.setString(3, fa.name); 
						favAuthorStmt.addBatch();
					}
					
					for (Integer fav_story_id : pe.myFavStories) {
						favStoryStmt.setInt(1, pe.my_ff_id); // my_ff_id
						favStoryStmt.setInt(2, fav_story_id); // story_id
						favStoryStmt.addBatch();
					}

					profileStmt.executeBatch();
					favAuthorStmt.executeBatch();
					favStoryStmt.executeBatch();

				} catch (BatchUpdateException e) {
					Boot.getLogger().log(e, "Failed to insert user with ff_id: " + pe.my_ff_id);
				} finally {
					profileStmt.close();
					favAuthorStmt.close();
					favStoryStmt.close();
				}
			}
		});
	}
}