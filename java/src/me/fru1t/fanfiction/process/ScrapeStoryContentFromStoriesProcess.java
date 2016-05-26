package me.fru1t.fanfiction.process;

import java.util.Stack;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.fanfiction.database.producers.StoryIdAndChaptersProducer;
import me.fru1t.fanfiction.database.producers.StoryIdAndChaptersProducer.StoryIdAndChapters;


public class ScrapeStoryContentFromStoriesProcess implements Runnable {
	private static final String BOOK_CHAPTER_URL_FMT = "https://www.fanfiction.net/s/%d/%d";
	private static final String SESSION_NAME =
			"Harry Potter, All English, 5-22-16, ScrapeStoryContentFromStoriesProcess, Fix 2";

	@Override
	public void run() {
		Boot.getLogger().log("Running ScrapeStoryContentFromStoriesProcess with Session Name " + SESSION_NAME);

		try {
			StoryIdAndChaptersProducer producer = new StoryIdAndChaptersProducer();
			producer.startAt(386);

			while (true) {
				StoryIdAndChapters storyInfo = producer.take();
				if (storyInfo == null) {
					Boot.getLogger().log("T-t-t-that's all folks -- No more books in queue");
					break;
				}

				Boot.getLogger().log("Scraping Story - "
						+ "id: " + storyInfo.id
						+ "; Story ID: " + storyInfo.storyId
						+ "; Chapters: " + storyInfo.chapters);

				Stack<String> chapterUrls = new Stack<>();
				for (int chapter = 1; chapter <= storyInfo.chapters; chapter++) {
					chapterUrls.add(String.format(BOOK_CHAPTER_URL_FMT, storyInfo.storyId, chapter));
				}

				while (!chapterUrls.isEmpty()) {
					String crawlUrl = chapterUrls.pop();
					String crawlContent = Boot.getCrawler().getContents(crawlUrl);
					if (crawlContent != null && crawlContent.length() == 16322) {
						Boot.getLogger().log("Story doesn't exist anymore, skipping.");
						break;
					}
					StoredProcedures.addScrape(SESSION_NAME, crawlUrl, crawlContent);
					Boot.getLogger().log("Page " + crawlUrl
							+ "; Content Length: " + ((crawlContent != null) ? crawlContent.length() : 0));
				}
			}
		} catch (InterruptedException ie) {
			Boot.getLogger().log(ie, "Stopping " + SESSION_NAME + " due to interrupt exception.");
		}
	}
}
