package me.fru1t.fanfiction.process.convert;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.util.Consumer;
import me.fru1t.util.DatabaseConnectionPool.Statement;

public class CategoryToFandoms extends Consumer<Scrape> {
	/**
	 * Simple Java class to store fandom information.
	 */
	private static class Fandom {
		public String name;
		public String url;

		public Fandom(String name, String url) {
			this.name = name;
			this.url = url;
		}

		@Override
		public String toString() {
			return "Fandom [name=" + name + ", url=" + url + "]";
		}
	}

	/**
	 * Regex pattern that matches category page urls, contining a single group which is the
	 * category. (eg. https://www.fanfiction.net/anime/ would successfully produce group 1: "anime"
	 */
	private static final Pattern CATEGORY_PAGE_URL_PATTERN =
			Pattern.compile("^https:\\/\\/www.fanfiction.net\\/([^\\/]+)\\/$");
	private static final int CATEGORY_NAME_GROUP = 1;

	private static final String FANDOM_LINK_SELECTOR = "#list_output table tbody tr td div a";
	private static final String FANDOM_STORY_COUNT_SELECTOR =
			"#list_output table tbody tr td div span.gray";
	private static final String FANDOM_LINK_ATTR = "href";

	/**
	 * 	1 `category_id` int(11) NOT NULL,
		2 `name` varchar(128) NOT NULL,
		3 `url` varchar(2000) NOT NULL,
	 */
	private static final String QUERY_INSERT_FANDOM = 
			"INSERT IGNORE INTO `fandom` (`category_id`, `name`, `url`) VALUES (?, ?, ?)";
	
	@Override
	public void eat(Scrape scrape) {
		// Verify scrape is a category page and fetch category
		Matcher m = CATEGORY_PAGE_URL_PATTERN.matcher(scrape.url);
		if (!m.matches()) {
			Boot.getLogger().log("CategoryToFandoms converter process was passed " + scrape.url
					+ " which isn't a category page. I've ignored it and moved on, but this means"
					+ "either a mistake was made when specifying the session to process, or two"
					+ "session names are the same.", true);
			return;
		}
		String category = m.group(CATEGORY_NAME_GROUP);

		// Get fandom info
		ArrayList<Fandom> fandoms = new ArrayList<>();
		Document categoryPageDoc = Jsoup.parse(scrape.content);
		Elements fandomEls = categoryPageDoc.select(FANDOM_LINK_SELECTOR);
		for (Element fandomEl : fandomEls) {
			String fandom_name = fandomEl.attr("title").replace("\\", "");
			if (!fandom_name.isEmpty())
				fandoms.add(new Fandom(fandom_name, fandomEl.attr(FANDOM_LINK_ATTR)));
		}

		// Get category story count info
		Elements countEls = categoryPageDoc.select(FANDOM_STORY_COUNT_SELECTOR);
		int storiesCount = 0;
		for (Element countEl : countEls) {
			int multiplier = 1;
			if (countEl.text().contains("K")) {
				multiplier = 1000;
			}
			storiesCount += Double.parseDouble(countEl.text().replaceAll("[^0-9.]", "")) * multiplier;
		}
		Boot.getLogger().log(category + ": " + storiesCount, true);

		// Store fandoms in database
		try {
			
			Boot.getDatabaseConnectionPool().executeStatement(new Statement() {
				@Override
				public void execute(Connection c) throws SQLException {
					CallableStatement categoryStmt = c.prepareCall ("{? = call fn_insfet_category(?)}");
				   	PreparedStatement fandomStmt = c.prepareStatement(QUERY_INSERT_FANDOM);
				   	
					try {
						categoryStmt.registerOutParameter (1, Types.INTEGER);
						categoryStmt.setString(2, category);    
						categoryStmt.execute();
					   	int category_id = categoryStmt.getInt(1);
					   	
					   	for (Fandom fandom : fandoms) {
					   		fandomStmt.setInt(1, category_id);
					   		fandomStmt.setString(2, fandom.name);
					   		fandomStmt.setString(3, fandom.url);
					   		fandomStmt.addBatch();
					   	}
					   	
					   	fandomStmt.executeBatch();
					} finally {
					   	if (categoryStmt != null) categoryStmt.close();
					   	if (fandomStmt != null) fandomStmt.close();
					}
				}
			});
			
		} catch (InterruptedException e) {
			Boot.getLogger().log(e, "Interrupt occured when trying to store fandoms into the database.");
		}
	}

}
