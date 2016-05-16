package me.fru1t.fanfiction.process;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.web.MultiIPCrawler;

public class ScrapeBooksListProcess implements Runnable {
	private static final String SESSION_NAME = "Dr Who, English, May 13 2016";
	private static final String CRAWL_URL = "https://www.fanfiction.net/tv/Doctor-Who/?&srt=2&lan=1&r=103&p=";
	
	private static final int MAX_PAGES = 2163;
	private static final int AVG_SLEEP_TIME_PER_IP = 7500;
	private static final byte[][] ips = {
			{ (byte) 104, (byte) 128, (byte) 237, (byte) 128 },
			{ (byte) 104, (byte) 128, (byte) 233, (byte) 73 },
			{ (byte) 45, (byte) 58, (byte) 54, (byte) 250 }
	};

	@Override
	public void run() {
		Boot.getLogger().log("Running scrapeBooksListProcess with session name: " + SESSION_NAME);
		try {
			// Non inclusive start page
			int page = 24;
			MultiIPCrawler crawler = new MultiIPCrawler(Boot.getLogger(), AVG_SLEEP_TIME_PER_IP, ips);
			while (page++ < MAX_PAGES) {
				String crawlUrl = CRAWL_URL + page;
				String crawlContent = crawler.getContents(crawlUrl);
				Boot.getLogger().log("Page " + crawlUrl + " Content Length: " + ((crawlContent != null) ? crawlContent.length() : 0));
				StoredProcedures.addScrape(SESSION_NAME, crawlUrl, crawlContent);
			}
		} catch (InterruptedException e) {
			Boot.getLogger().log(e, "ScrapeBooksListProcess session " + SESSION_NAME + " was interrupted by:");
		}
	}

}
