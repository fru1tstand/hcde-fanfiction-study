package me.fru1t.fanfiction.process;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.Session;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.util.concurrent.GenericProducer;

/**
 * Scrapes whatever urls are given from a producer into the database.
 */
public class ScrapeProcess implements Runnable {
	private GenericProducer<String> urlProducer;
	private Session session;

	/**
	 * Creates a new scrape process given the urls in the form of a producer and the session name.
	 * @param urlProducer
	 */
	public ScrapeProcess(GenericProducer<String> urlProducer, Session session) {
		this.urlProducer = urlProducer;
		this.session = session;
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ScrapeProcess with session name: " + session);
		try {
			String crawlUrl = urlProducer.take();
			while (crawlUrl != null) {
				String crawlContent = Boot.getCrawler().getContents(crawlUrl);
				StoredProcedures.addScrape(session, crawlUrl, crawlContent);
				crawlUrl = urlProducer.take();
			}
		} catch (InterruptedException e) {
			Boot.getLogger().log(e, "Scrape process '" + session + "' was interrupted by:");
		}
	}
}
