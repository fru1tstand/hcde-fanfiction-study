package me.fru1t.fanfiction.process;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.util.concurrent.GenericProducer;

public class ScrapeProcess implements Runnable {
	private GenericProducer<String> urlProducer;
	private String sessionName;

	/**
	 * Creates a new scrape process given the urls in the form of a producer and the session name.
	 * @param urlProducer
	 */
	public ScrapeProcess(GenericProducer<String> urlProducer, String sessionName) {
		this.urlProducer = urlProducer;
		this.sessionName = sessionName;
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ScrapeProcess with session name: " + sessionName);
		try {
			String crawlUrl = urlProducer.take();
			while (crawlUrl != null) {
				String crawlContent = Boot.getCrawler().getContents(crawlUrl);
				Boot.getLogger().log("Crawled: " + crawlUrl
						+ " Length: " + ((crawlContent != null) ? crawlContent.length() : 0));
				StoredProcedures.addScrape(sessionName, crawlUrl, crawlContent);
				crawlUrl = urlProducer.take();
			}
		} catch (InterruptedException e) {
			Boot.getLogger().log(e, "Scrape process '" + sessionName + "' was interrupted by:");
		}
	}
}
