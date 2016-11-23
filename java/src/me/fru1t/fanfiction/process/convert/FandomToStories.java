package me.fru1t.fanfiction.process.convert;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoryListProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.web.page.StoryListPage;
import me.fru1t.util.Consumer;

/**
 * This class defines a consumer which eats fandom pages and stores their contained stories within
 * the database.
 */

public class FandomToStories extends Consumer<Scrape> {

	// Matches fandom URLs with or without filters.
	private static final Pattern STORY_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/(?<categoryName>[^/]+)/(?<fandomName>[^/]+)/(?<filters>.*)$");


	@Override
	public void eat(Scrape scrape) {
		long startTime = (new Date()).getTime();

		// Check for scrape URL validity.
		Matcher m = STORY_URL_PATTERN.matcher(scrape.url);
		if (!m.matches()) {
			Boot.getLogger().log("Invalid URL for Fandom, ignoring: " + scrape.url, true);
			return;
		}
		
		String category_name = m.group("categoryName");
		String fandom_name = m.group("fandomName");
		String fandomUrl_inFandom = String.format("/%s/%s/", category_name, fandom_name);

		try {
			// FandomPage fandomPage = new FandomPage(scrape.content);
			StoryListPage storyListPage = new StoryListPage(scrape.content);
			StoryListProcedures.addStoryValues(scrape.url, fandomUrl_inFandom, storyListPage.getStoryElements());

			Boot.getLogger().log("Processed scrape with ID: " + scrape.id +  " / " + scrape.url
					+ "; Found: " + storyListPage.getStoryElements().size() + " stories"
					+ "; Took: " + ((new Date()).getTime() - startTime) + "ms", false);
		} catch (Exception e) {
			// Boot.getLogger().log(e, "Skipped scrape with ID " + scrape.id + " due to:");
			Boot.getLogger().log(e, "Exiting on scrape with ID " + scrape.id + " due to:");
			System.exit(42);
		}
	}
}
