package me.fru1t.fanfiction.database;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.ProfileElement;
import me.fru1t.fanfiction.web.page.element.ProfileElement.FavAuthor;
import me.fru1t.util.DatabaseConnectionPool.Statement;

public class UserProcedures {
	

	/**
	 *   1 in_ff_id INT,
	 *   2 in_name VARCHAR(512),
	 *   3 in_location_name VARCHAR(128),
	 *   4 in_join_date INT(10),
	 *   5 in_update_date INT(10),
	 *   6 in_bio MEDIUMTEXT,
	 *   7 in_age TINYINT,
	 *   8 in_gender CHAR(6)
	 */
	private static final String USP_ADD_USER_AND_USER_PROFILE 
	  = "{CALL usp_add_user_and_user_profile (?,?,?,?,?, ?,?,?)}";
	
	/**
	 *  1 `user_id` INT(11) NOT NULL,
	 *  2 `favorite_user_id` INT(11) NOT NULL
	 */
	private static final String INSERT_QUERY_USER_FAV_AUTHOR = 
			"INSERT INTO `user_favorite_author` (`user_id`, `favorite_user_id`) VALUES (?, ?)";

	/**
	 *  1 `user_id` INT(11) NOT NULL,
	 *  2 `story_id` INT NOT NULL,
	 */
	private static final String INSERT_QUERY_USER_FAV_STORY = 
			"INSERT INTO `user_favorite_story` (`user_id`, `story_id`) VALUES (?, ?)";
	 

	public static void addUserProfile(ArrayList<ProfileElement> profileElements)  throws InterruptedException {
		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				CallableStatement userStmt = c.prepareCall(USP_ADD_USER_AND_USER_PROFILE);

				try {
					c.setAutoCommit(false);

					// ADD to `user` and `user_profile`
					for (ProfileElement pe : profileElements) {
						userStmt.setInt(1, pe.my_ff_id); // in_ff_id
						userStmt.setString(2, pe.user_name); // in_name
						// this converts to location_id at the database procedural level
						if (pe.location_name != null && !pe.location_name.isEmpty()) // location_name
							userStmt.setString(3, pe.location_name);
						else userStmt.setString(3,  null);
						userStmt.setInt(4, pe.join_date); // join_date
						userStmt.setInt(5, pe.update_date); // update_date
						userStmt.setString(6, pe.bio); // bio
						if (pe.age > 0) userStmt.setInt(7, pe.age); // age
						else userStmt.setNull(7, Types.INTEGER);
						if (pe.gender != null && !pe.gender.isEmpty()) // gender
							userStmt.setString(8, pe.gender); 
						else userStmt.setString(8, null);
						userStmt.addBatch();
					}
					userStmt.executeBatch();
					
					
					c.commit();

				} catch (BatchUpdateException e) {
					String filename =  "userurl" + (new Date()).getTime() + ".txt";

					Boot.getLogger().log(e, "processUserScrapeToProfileBatch is having trouble! " 
							+ "Outputting a list of urls that were supposed to be inserted to a file \""
							+ filename);

					try {
						BufferedWriter fileWriter = 
								new BufferedWriter(new FileWriter(filename, true));
						for (ProfileElement pe : profileElements) {
							fileWriter.write(pe.getScrapeId() + "\r\n");
							fileWriter.flush();
						}
						fileWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						Boot.getLogger().log(e1, "Couldn't write to file \"" + filename);
					}
				} finally {
					userStmt.close();
				}
			}
		});
	}
	

	/**
	 * This should be done after adding all the users to `user` and `user_profile` 
	 * (i.e. do addUserProfile first before this)
	 * @param profileElements
	 * @throws InterruptedException
	 */
	public static void addUserFavorites(ArrayList<ProfileElement> profileElements)  throws InterruptedException {
		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				PreparedStatement selectStmt = c.prepareStatement("");
				PreparedStatement favAuthorStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_AUTHOR);
				PreparedStatement favStoryStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_STORY);

				try {
					c.setAutoCommit(false);

					// GET User IDs
					ArrayList<String> ffIds = new ArrayList<>();
					for (ProfileElement pe : profileElements) { 
						ffIds.add(Integer.toString(pe.my_ff_id));
						for (FavAuthor fa : pe.myFavAuthors) {
							ffIds.add(Integer.toString(fa.ff_id));
						}
					}
					String selectUserQuery = String.format(
							"SELECT ff_id AS ffId, id AS userId FROM `user` WHERE ff_id in (%s)", 
							String.join(",", ffIds));
					ResultSet rs = selectStmt.executeQuery(selectUserQuery);
					HashMap<Integer, Integer> ffId_to_userId = new HashMap<>();
				    while (rs.next()) ffId_to_userId.put(rs.getInt("ffId"), rs.getInt("userId"));
				    
				    // GET Story IDs
					ArrayList<String> ffStoryIds = new ArrayList<>();
					for (ProfileElement pe : profileElements) { 
						for (Integer fav_ff_story_id : pe.myFavStories) {
							ffStoryIds.add(Integer.toString(fav_ff_story_id));
						}
					}
					String selectStoryQuery = String.format(
							"SELECT ff_story_id AS ffStoryId, id AS storyId FROM `story` WHERE ff_story_id in (%s)", 
							String.join(",", ffStoryIds));
					rs = selectStmt.executeQuery(selectStoryQuery);
					HashMap<Integer, Integer> ffStoryId_to_storyId = new HashMap<>();
					while (rs.next()) ffStoryId_to_storyId.put(rs.getInt("ffStoryId"), rs.getInt("storyId")); 
					
					// ADD to `favorite_story` and `favorite_author`
					for (ProfileElement pe : profileElements) {
						int my_user_id = ffId_to_userId.get(pe.my_ff_id);
						
						for (FavAuthor fav : pe.myFavAuthors) {
							Integer fav_user_id = ffId_to_userId.get(fav.ff_id);
							if (fav_user_id != null) { // favorited-user is in the DB
								favAuthorStmt.setInt(1, my_user_id);
								favAuthorStmt.setInt(2, fav_user_id);  
								favAuthorStmt.addBatch();
							}
						}

						for (Integer fav_ff_story_id : pe.myFavStories) {
							Integer fav_story_id = ffStoryId_to_storyId.get(fav_ff_story_id);
							if (fav_story_id != null) {
								favStoryStmt.setInt(1, my_user_id);
								favStoryStmt.setInt(2, fav_story_id); // story_id
								favStoryStmt.addBatch();
							} 
							// we are only going to consider stories that are in the `story` table.
							// stories that we do not have in `story` are likely to be the 
							// cross-over stories, which we are not going to take into consideration
						}
						
					}

					favAuthorStmt.executeBatch();
					favStoryStmt.executeBatch();
					
					c.commit();

				} catch (BatchUpdateException e) {
					String filename =  "userurl" + (new Date()).getTime() + ".txt";

					Boot.getLogger().log(e, "processUserScrapeToProfileBatch is having trouble! " 
							+ "Outputting a list of urls that were supposed to be inserted to a file \""
							+ filename);

					try {
						BufferedWriter fileWriter = 
								new BufferedWriter(new FileWriter(filename, true));
						for (ProfileElement pe : profileElements) {
							fileWriter.write(pe.getScrapeId() + "\r\n");
							fileWriter.flush();
						}
						fileWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						Boot.getLogger().log(e1, "Couldn't write to file \"" + filename);
					}
				} finally {
					selectStmt.close();
					favAuthorStmt.close();
					favStoryStmt.close();
				}
			}
		});
	}
}