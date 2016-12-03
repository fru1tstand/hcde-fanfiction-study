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

public class UserToProfileProcedures {
	
	
	/**
	 *   1 `user_id` INT NOT NULL,
	 *   2 `location_id` INT NULL,
	 *   3 `join_date` INT(10) DEFAULT -1,
	 *   4 `update_date` INT(10) DEFAULT -1,
	 *   5 `bio` MEDIUMTEXT NULL,
	 *   6 `age` TINYINT NULL,
	 *   7 `gender` CHAR(6) NULL,
	 */
	private static final String USP_ADD_USER_PROFILE = "{CALL usp_add_user_profile (?,?,?,?, ?,?,?)}";
	
	/**
	 *  1 `user_id` INT(11) NOT NULL,
	 *  2 `favorite_user_id` INT(11) NOT NULL
	 */
	private static final String INSERT_QUERY_USER_FAV_AUTHOR = 
			"INSERT INTO `user_favorite_author` (`user_id`, `favorite_user_id`) VALUES (?, ?)";

	/**
	 *  1 `user_id` INT(11) NOT NULL,
	 *  2 `favorite_ff_id` INT(11) NOT NULL,
	 *  3 `favorite_name` varchar(128) NOT NULL,
	 */
	private static final String INSERT_QUERY_USER_FAV_AUTHOR_DUMMY = 
			"INSERT INTO `user_favorite_author_dummy` (`user_id`, `favorite_ff_id`, `favorite_name`) VALUES (?, ?, ?)";
	
	/**
	 *  1 `user_id` INT(11) NOT NULL,
	 *  2 `story_id` INT NOT NULL,
	 */
	private static final String INSERT_QUERY_USER_FAV_STORY = 
			"INSERT INTO `user_favorite_story` (`user_id`, `story_id`) VALUES (?, ?)";
	
	public static void addUserProfile ( ArrayList<ProfileElement> peList ) throws InterruptedException {
		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				
				PreparedStatement selectStmt = c.prepareStatement("");
				CallableStatement profileStmt = c.prepareCall(USP_ADD_USER_PROFILE);
				PreparedStatement favAuthorStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_AUTHOR);
				PreparedStatement favStoryStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_STORY);
				PreparedStatement favAuthorDummyStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_AUTHOR_DUMMY);
				

				try {
					c.setAutoCommit(false);
					
					// GET User IDs
					ArrayList<String> ffIds = new ArrayList<>();
					for (ProfileElement pe : peList) { 
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
					for (ProfileElement pe : peList) { 
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
				    
				    
				    // BATCH insert profile, fav_author and fav_story
					for (ProfileElement pe : peList) {
						if (pe == null) {
							Boot.getLogger().log("Skipped: PE is null.", true);
							continue;
						}
						
						int my_user_id = ffId_to_userId.get(pe.my_ff_id);
						profileStmt.setInt(1, my_user_id); // user_id
						
						// this converts to location_id at the database procedural level
						if (pe.location_name != null && !pe.location_name.isEmpty())
							profileStmt.setString(2, pe.location_name); // location_name
						else
							profileStmt.setString(2,  null);
						
						profileStmt.setInt(3, pe.join_date); // join_date
						profileStmt.setInt(4, pe.update_date); // update_date
						
						profileStmt.setString(5, pe.bio); // bio
						
						if (pe.age > 0)
							profileStmt.setInt(6, pe.age); // age
						else
							profileStmt.setNull(6, Types.INTEGER);
						
						if (pe.gender != null && !pe.gender.isEmpty())
							profileStmt.setString(7, pe.gender); // gender
						else
							profileStmt.setString(7, null);
						
						profileStmt.addBatch();

						for (FavAuthor fav : pe.myFavAuthors) {
							Integer fav_user_id = ffId_to_userId.get(fav.ff_id);
							if (fav_user_id != null) { // favorited-user is in the DB
								favAuthorStmt.setInt(1, my_user_id);
								favAuthorStmt.setInt(2, fav_user_id);  
								favAuthorStmt.addBatch();
							} else {
								favAuthorDummyStmt.setInt(1, my_user_id);
								favAuthorDummyStmt.setInt(2, fav.ff_id); // favorite_user_id
								favAuthorDummyStmt.setString(3, fav.name); 
								favAuthorDummyStmt.addBatch();
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

					profileStmt.executeBatch();
					favAuthorStmt.executeBatch();
					favAuthorDummyStmt.executeBatch();
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
						for (ProfileElement pe : peList) {
							fileWriter.write(pe.getScrapeId() + "\r\n");
							fileWriter.flush();
						}
						fileWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						Boot.getLogger().log(e1, "Couldn't write to file \"" + filename);
					}
				} finally {
					profileStmt.close();
					favAuthorStmt.close();
					favStoryStmt.close();
				}
			}
		});
	}
	
	
	/******************************************************************************************************/
	
	private static final String INSERT_QUERY_USER_PROFILE_BATCH = 
			"INSERT IGNORE INTO `user_profile_batch` (`ff_id`, `location_name`, `join_date`, "
			+ "`update_date`, `bio`, `age`, `gender`)"  
			+ " VALUES (?, ?, ?, ?, ?, ?, ?)";
	
	/**
	 *  1 `ff_id` INT(11) NOT NULL,
	 *  2 `favorite_ff_id` INT(11) NOT NULL,
	 *  3 `favorite_name` varchar(128) NOT NULL,
	 */
	private static final String INSERT_QUERY_USER_FAV_AUTHOR_BATCH = 
			"INSERT IGNORE INTO `user_favorite_author_batch` (`ff_id`, `favorite_ff_id`, `favorite_name`)"  
			+ " VALUES (?, ?, ?)";
	
	/**
	 *  1 `ff_id` INT(11) NOT NULL,
	 *  2 `story_id` INT NOT NULL,
	 */
	private static final String INSERT_QUERY_USER_FAV_STORY_BATCH = 
			"INSERT IGNORE INTO `user_favorite_story_batch` (`ff_id`, `ff_story_id`)"  
			+ " VALUES (?, ?)";
	
	public static void addUserProfileBatch ( 
			ArrayList<ProfileElement> peList ) throws InterruptedException {
		Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
			@Override
			public void execute(Connection c) throws SQLException {
				PreparedStatement profileStmt = c.prepareCall(INSERT_QUERY_USER_PROFILE_BATCH);
				PreparedStatement favAuthorStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_AUTHOR_BATCH);
				PreparedStatement favStoryStmt = c.prepareStatement(INSERT_QUERY_USER_FAV_STORY_BATCH);

				try {
					for (ProfileElement pe : peList) {
						if (pe == null) {
							Boot.getLogger().log("Skipped: PE is null.", true);
							continue;
						}
						
						profileStmt.setInt(1, pe.my_ff_id); // in_ff_id
						
						if (pe.location_name != null && !pe.location_name.isEmpty())
							profileStmt.setString(2, pe.location_name); // location_name
						else
							profileStmt.setString(2,  null);
						
						if (pe.join_date > 0)
							profileStmt.setInt(3, pe.join_date); // join_date
						else
							profileStmt.setNull(3, Types.INTEGER); 
						
						if (pe.update_date > 0)
							profileStmt.setInt(4, pe.update_date); // update_date
						else
							profileStmt.setNull(4, Types.INTEGER);
						
						if (pe.bio != null && !pe.bio.isEmpty())
							profileStmt.setString(5, pe.bio); // bio
						else
							profileStmt.setString(5, null);
						
						if (pe.age > 0)
							profileStmt.setInt(6, pe.age); // age
						else
							profileStmt.setNull(6, Types.INTEGER);
						
						if (pe.gender != null && !pe.gender.isEmpty())
							profileStmt.setString(7, pe.gender); // gender
						else
							profileStmt.setString(7, null);
						
						profileStmt.addBatch();

						for (FavAuthor fa : pe.myFavAuthors) {
							favAuthorStmt.setInt(1, pe.my_ff_id);
							favAuthorStmt.setInt(2, fa.ff_id); // favorite_user_id
							favAuthorStmt.setString(3, fa.name); 
							favAuthorStmt.addBatch();
						}

						for (Integer fav_story_id : pe.myFavStories) {
							favStoryStmt.setInt(1, pe.my_ff_id);
							favStoryStmt.setInt(2, fav_story_id); // story_id
							favStoryStmt.addBatch();
						}
					}

					profileStmt.executeBatch();
					favAuthorStmt.executeBatch();
					favStoryStmt.executeBatch();

				} catch (BatchUpdateException e) {
					String filename =  "userurl" + (new Date()).getTime() + ".txt";

					Boot.getLogger().log(e, "processUserScrapeToProfileBatch is having trouble! " 
							+ "Outputting a list of urls that were supposed to be inserted to a file \""
							+ filename);

					try {
						BufferedWriter fileWriter = 
								new BufferedWriter(new FileWriter(filename, true));
						for (ProfileElement pe : peList) {
							fileWriter.write(pe.getScrapeId() + "\r\n");
							fileWriter.flush();
						}
						fileWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
						Boot.getLogger().log(e1, "Couldn't write to file \"" + filename);
					}
				} finally {
					profileStmt.close();
					favAuthorStmt.close();
					favStoryStmt.close();
				}
			}
		});
	}
	

	
	/**
	 * 1 in_ff_id int(11), not null
	 * 2 in_user_name varchar(128), not null
     * 3 in_location_name VARCHAR(128),
     * 4 in_join_date int(10),
     * 5 in_update_date int(10),
     * 6 in_bio mediumtext,
     * 7 in_age tinyint(4),
     * 8 in_gender char(6)
	 */
	@Deprecated
	private static final String USP_ADD_USER_PROFILE_RELAX =
			"{CALL usp_add_user_profile_relax(?,?,?,?,?,?,?,?,?)}";
	
	/**
	 *  1 `ff_id` INT(11) NOT NULL,
	 *  2 `favorite_ff_id` INT(11) NOT NULL,
	 *  3 `favorite_name` varchar(128) NOT NULL,
	 */
	@Deprecated
	private static final String INSERT_QUERY_USER_FAV_AUTHOR_RELAX = 
			"INSERT IGNORE INTO `user_favorite_author_relax` (`ff_id`, `favorite_ff_id`, `favorite_name`)"  
			+ " VALUES (?, ?, ?)";
	
	/**
	 *  1 `ff_id` INT(11) NOT NULL,
	 *  2 `story_id` INT NOT NULL,
	 */
	@Deprecated
	private static final String INSERT_QUERY_USER_FAV_STORY_RELAX = 
			"INSERT IGNORE INTO `user_favorite_story_relax` (`ff_id`, `ff_story_id`)"  
			+ " VALUES (?, ?)";
	
	@Deprecated
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
					profileStmt.registerOutParameter(2, java.sql.Types.INTEGER); // out_user_id
					profileStmt.setString(3, pe.user_name); // user_name
					profileStmt.setString(4, pe.location_name); // location_name
					profileStmt.setInt(5, pe.join_date); // join_date
					profileStmt.setInt(6, pe.update_date); // update_date
					profileStmt.setString(7, pe.bio); // bio
					profileStmt.setInt(8, pe.age); // age
					profileStmt.setString(9, pe.gender); // gender
					profileStmt.execute();
					int user_id = profileStmt.getInt(2);

					for (FavAuthor fa : pe.myFavAuthors) {
						favAuthorStmt.setInt(1, user_id);
						favAuthorStmt.setInt(2, fa.ff_id); // favorite_user_id
						favAuthorStmt.setString(3, fa.name); 
						favAuthorStmt.addBatch();
					}

					for (Integer fav_story_id : pe.myFavStories) {
						favStoryStmt.setInt(1, user_id);
						favStoryStmt.setInt(2, fav_story_id); // story_id
						favStoryStmt.addBatch();
					}

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