package me.fru1t.fanfiction.process;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.Database;
import me.fru1t.web.Crawler;

public class ScrapeBooksListProcess implements Runnable {
	private static final String SESSION_ID = "Harry Potter, English, March 29, 2016";
	private static final String CRAWL_URL = "https://www.fanfiction.net/book/Harry-Potter/?&srt=2&lan=1&r=103&p=";
	
	private static final int MAX_PAGES = 17996;
	private static final int SLEEP_TIME_SECONDS = 10;

	@Override
	public void run() {
		int i = 0;
		int crawlContentLength = 0;
		String crawlContent = null;
		
		while (i < MAX_PAGES) {
			i++;
			crawlContent = Crawler.getContents(CRAWL_URL + i);
			Database.insertRawScrape(SESSION_ID, CRAWL_URL + i, crawlContent);
			crawlContentLength = (crawlContent != null) ? crawlContent.length() : 0;
			Boot.log("Page #" + i + " Content Length: " + crawlContentLength);
			try {
				Thread.sleep(SLEEP_TIME_SECONDS * 1000);
			} catch (InterruptedException e) {
				Boot.log(e, null);
				return;
			}
		}
	}

}