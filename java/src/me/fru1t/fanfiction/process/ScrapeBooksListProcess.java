package me.fru1t.fanfiction.process;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoredProcedures;

public class ScrapeBooksListProcess implements Runnable {
	private static final String SESSION_NAME = "Harry Potter, Rated M, English, 5-22-16, Fix 2";
	private static final String CRAWL_URL = "https://www.fanfiction.net/book/Harry-Potter/?&srt=2&lan=1&r=4&p=";

	private static final int MAX_PAGES = 4447;


	@Override
	public void run() {
		Boot.getLogger().log("Running scrapeBooksListProcess with session name: " + SESSION_NAME);
		try {
			// Non inclusive start page
			int page = 0;
			while (page++ < MAX_PAGES) {
				String crawlUrl = CRAWL_URL + page;
				String crawlContent = Boot.getCrawler().getContents(crawlUrl);
				Boot.getLogger().log("Page " + crawlUrl + " Content Length: " + ((crawlContent != null) ? crawlContent.length() : 0));
				StoredProcedures.addScrape(SESSION_NAME, crawlUrl, crawlContent);
			}
		} catch (InterruptedException e) {
			Boot.getLogger().log(e, "ScrapeBooksListProcess session " + SESSION_NAME + " was interrupted by:");
		}
	}

}
