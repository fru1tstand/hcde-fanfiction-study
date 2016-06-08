package me.fru1t.fanfiction.process.convert;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.Session;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.web.page.FandomPage;
import me.fru1t.util.Consumer;

/**
 * This class defines a consumer which eats fandom pages and stores their contained stories within
 * the database.
 */
public class FandomToStories extends Consumer<Scrape> {
	// Matches fandom URLs with or without filters.
	private static final Pattern STORY_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/([^/]+)/([^/]+)/(.*)$");

	// The base fanfiction.net url.
	private static final String BASE_FFN_URL = "https://www.fanfiction.net/";

	// The group number for the Category name within the story_url_pattern.
	private static final int CATEGORY_GROUP = 1;

	// The group number for the Fandom name within the story_url_pattern.
	private static final int FANDOM_GROUP = 2;

	// The group number for the filter content within the story_url_pattern.
	private static final int FILTERS_GROUP = 3;


	private Session convertSession;

	public FandomToStories(Session convertSession) {
		this.convertSession = convertSession;
	}

	@Override
	public void eat(Scrape scrape) {
		long startTime = (new Date()).getTime();

		// Check for scrape URL validity.
		Matcher m = STORY_URL_PATTERN.matcher(scrape.url);
		if (!m.matches()) {
			Boot.getLogger().log("Invalid URL for Fandom, ignoring: " + scrape.url);
			return;
		}
		String fandomUrl = BASE_FFN_URL
				+ "/" + m.group(CATEGORY_GROUP)
				+ "/" + m.group(FANDOM_GROUP) + "/";

		try {
			FandomPage fandomPage = new FandomPage(scrape.content);
			StoredProcedures.processListScrapeToStory(
					scrape.id,
					convertSession,
					m.group(CATEGORY_GROUP),
					fandomUrl,
					fandomPage.getStoryElements());

			Boot.getLogger().log("Processed /" + m.group(CATEGORY_GROUP)
					+ "/" + m.group(FANDOM_GROUP)
					+ "/" + m.group(FILTERS_GROUP)
					+ "; Found: " + fandomPage.getStoryElements().size() + " stories"
					+ "; Took: " + ((new Date()).getTime() - startTime) + "ms");
		} catch (Exception e) {
			Boot.getLogger().log(e, "Skipped scrape with ID " + scrape.id + " due to:");
		}
	}
}
