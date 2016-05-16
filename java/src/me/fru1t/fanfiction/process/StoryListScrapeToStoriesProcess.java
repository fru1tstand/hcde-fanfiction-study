package me.fru1t.fanfiction.process;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.web.page.FandomStoryListPage;


public class StoryListScrapeToStoriesProcess implements Runnable {
	private static final String PROCESS_SESSION_NAME =
			"All English MLP, HP, and DW - Storylist scrape to story - May 14 2016 - fix 2";
	@Nullable
	private static final String[] SCRAPE_SESSION_NAMES = {
			"My Little Pony, English, May 13 2016",
			"Harry Potter, English, May 13 2016",
			"Dr Who, English, May 13 2016"
			};

	private static final Pattern STORY_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/([^/]+)/([^/]+)/.+$");
	
	private static final int MATCHER_CATEGORY_NAME_GROUP = 1;
	
	@Override
	public void run() {
		Boot.getLogger().log("Running StoryListScrapeToStoriesProcess with Session: " + PROCESS_SESSION_NAME);
		ScrapeProducer sp = new ScrapeProducer(1050, -1, SCRAPE_SESSION_NAMES);
		Scrape scrape = sp.take();
		while (scrape != null) {
			Matcher m = STORY_URL_PATTERN.matcher(scrape.url);
			if (!m.matches()) {
				Boot.getLogger().log("Couldn't match scrape URL to pattern: " + scrape.url);
				scrape = sp.take();
				continue;
			}
			
			try {
				FandomStoryListPage fslp = new FandomStoryListPage(scrape.content);
				StoredProcedures.processListScrapeToStory(
						scrape.id,
						PROCESS_SESSION_NAME,
						m.group(MATCHER_CATEGORY_NAME_GROUP),
						fslp.getBookResultElements());
				Boot.getLogger().log("Processed scrape id: " + scrape.id + " with url: " + scrape.url);
			} catch (Exception e) {
				Boot.getLogger().log(e, "Skipped scrape with ID " + scrape.id + " due to:");
			}
			scrape = sp.take();
		}
	}
}
