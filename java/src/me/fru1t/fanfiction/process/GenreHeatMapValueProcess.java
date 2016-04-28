package me.fru1t.fanfiction.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.Database;
import me.fru1t.fanfiction.database.schema.scrape.StoryForHeatMapProducer;
import me.fru1t.fanfiction.database.schema.scrape.StoryForHeatMapProducer.Story;

/**
 * This process iterates over all entries in the scrape_book_result_ff_genre table to make a map
 * of what genres are paired with each other. It outputs unordered rows (genres) of ordered
 * columns (genres) with their respective genre-genre match count.
 */
public class GenreHeatMapValueProcess implements Runnable {
	private static final int STORIES_FETCHED = 10000;
	private static final String COL_BOOK_ID = "bookId";
	private static final String COL_GENRE = "genre";

	@Override
	public void run() {
		Boot.getLogger().log("Running GenreHeatMapValueProcess");
		
		HashMap<String, TreeMap<String, Long>> genreMap = new HashMap<>();
		StoryForHeatMapProducer storyProducer =
				new StoryForHeatMapProducer(Database.getConnection(), Boot.getLogger());
		
		while (true) {
			List<String> booksToSelect = new ArrayList<>();
			Story story;
			for (int i = 0; i < STORIES_FETCHED; i++) {
				story = storyProducer.take();
				if (story == null) {
					break;
				}
				booksToSelect.add(Integer.toString(story.id));
			}
			
			if (booksToSelect.size() < 1) {
				break;
			}

			HashMap<Integer, List<String>> genreList = new HashMap<>();
			try {
				PreparedStatement stmt = Database.getConnection().prepareStatement(
						"SELECT"
						+ " `scrape_book_result_ff_genre`.`ff_book_id` AS `" + COL_BOOK_ID + "`,"
						+ " `ff_genre`.`name` AS `" + COL_GENRE + "`"
						+ " FROM `scrape_book_result_ff_genre`"
						+ " INNER JOIN `ff_genre` ON `ff_genre`.`id` = `scrape_book_result_ff_genre`.`ff_genre_id`"
						+ " WHERE `scrape_book_result_ff_genre`.`ff_book_id`"
							+ " IN (" + String.join(",", booksToSelect) + ")");
				ResultSet result = stmt.executeQuery();
				int bookId;
				String genre;
				while (result.next()) {
					bookId = result.getInt(COL_BOOK_ID);
					genre = result.getString(COL_GENRE);
					if (!genreList.containsKey(bookId)) {
						genreList.put(bookId, new ArrayList<String>());
					}
					genreList.get(bookId).add(genre);
				}
				result.close();
				stmt.close();
			} catch (SQLException e) {
				Boot.getLogger().log(e);
				return;
			}
			int mappingsAdded = 0;
			for (List<String> pairedGenres : genreList.values()) {
				for (String g1 : pairedGenres) {
					if (!genreMap.containsKey(g1)) {
						genreMap.put(g1, new TreeMap<String, Long>());
						genreMap.get(g1).put("Adventure", 0L);
						genreMap.get(g1).put("Angst", 0L);
						genreMap.get(g1).put("Comfort", 0L);
						genreMap.get(g1).put("Crime", 0L);
						genreMap.get(g1).put("Drama", 0L);
						genreMap.get(g1).put("Family", 0L);
						genreMap.get(g1).put("Fantasy", 0L);
						genreMap.get(g1).put("Friendship", 0L);
						genreMap.get(g1).put("Horror", 0L);
						genreMap.get(g1).put("Humor", 0L);
						genreMap.get(g1).put("Hurt", 0L);
						genreMap.get(g1).put("Mystery", 0L);
						genreMap.get(g1).put("Parody", 0L);
						genreMap.get(g1).put("Poetry", 0L);
						genreMap.get(g1).put("Romance", 0L);
						genreMap.get(g1).put("Sci-Fi", 0L);
						genreMap.get(g1).put("Spiritual", 0L);
						genreMap.get(g1).put("Supernatural", 0L);
						genreMap.get(g1).put("Suspense", 0L);
						genreMap.get(g1).put("Tragedy", 0L);
						genreMap.get(g1).put("Western", 0L);
					}
					for (String g2 : pairedGenres) {
						if (g1 == g2) {
							continue;
						}
						mappingsAdded++;
						genreMap.get(g1).put(g2, genreMap.get(g1).get(g2) + 1);
					}
				}
			}
			Boot.getLogger().log("Added " + mappingsAdded + " mappings");
		}
		Boot.getLogger().log("\r\n\r\n\r\n==============RESULTS=============\r\n\r\n\r\n");
		Boot.getLogger().log("Baseline:");
		Boot.getLogger().log("Adventure\tAngst\tComfort\tCrime\tDrama\tFamily\tFantasy\tFriendship\tHorror\tHumor\tHurt\tMystery\tParody\tPoetry\tRomance\tSci-Fi\tSpiritual\tSupernatural\tSuspense\tTragedy\tWestern\r\n\r\n");
		String s1;
		String s2;
		for (Map.Entry<String, TreeMap<String, Long>> rows : genreMap.entrySet()) {
			Boot.getLogger().log(rows.getKey() + ":");
			s1 = "";
			s2 = "";
			for (Map.Entry<String, Long> cols : rows.getValue().entrySet()) {
				s1 += cols.getKey() + "\t";
				s2 += cols.getValue() + "\t";
			}
			Boot.getLogger().log(s1);
			Boot.getLogger().log(s2);
			Boot.getLogger().log("");
		}
	}
}
