package me.fru1t.fanfiction.process;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.Session;
import me.fru1t.fanfiction.Session.SessionName;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.util.Consumer;
import me.fru1t.util.ThreadUtils;
import me.fru1t.util.concurrent.ConcurrentProducer;
import me.fru1t.web.Request;

/**
 * Scrapes whatever urls are given from a producer into the database.
 */
public class ScrapeProcess implements Runnable {
	private static final int WATCHDOG_SLEEP_TIME_MS = 500;

	private ConcurrentProducer<String> urlProducer;
	private Session session;
	private String[] queuedCrawlUrl;

	/**
	 * Creates a new scrape process given the urls in the form of a producer and the session name.
	 * @param urlProducer
	 * @throws InterruptedException 
	 */
	public ScrapeProcess(ConcurrentProducer<String> urlProducer, SessionName sessName) throws InterruptedException {
		this.urlProducer = urlProducer;
		this.session = new Session(sessName);
		this.queuedCrawlUrl = new String[1];
		this.queuedCrawlUrl[0] = null;
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ScrapeProcess with session name: " + session.getName() 
							+ "\n\t with ID number " + session.getID() 
							+ "\n\t on DB " +  Boot.database, true);

		// Startup loop, saturate queue by calling until it returns false.
		while (queueNextScrape());

		// Watchdog for when we reach the end of a fandom
		while (!urlProducer.isComplete() && !Thread.currentThread().isInterrupted()) {
			if (Boot.getCrawler().countFreeIps() > 0) {
				while (queueNextScrape());
			}

			try {
				ThreadUtils.waitGauss(WATCHDOG_SLEEP_TIME_MS);
			} catch (InterruptedException e) {
				Boot.getLogger().log("Watchdog was interrupted and is shutting down all children", true);
				return;
			}
		}

		Boot.getLogger().log("Completed ScrapeProcess with session name: " + session.getName(), true);
	}

	private synchronized boolean queueNextScrape() {
		synchronized (queuedCrawlUrl) {
			if (queuedCrawlUrl[0] == null) {
				queuedCrawlUrl[0] = urlProducer.take();
			}
			if (queuedCrawlUrl[0] == null) {
				return false;
			}
			String tCrawlUrl = queuedCrawlUrl[0];
			if (Boot.getCrawler().sendRequest(new Request(tCrawlUrl, new Consumer<String>() {
					@Override
					public void eat(String crawlContent) {
						// If we're super fast, this might happen.
						synchronized (queuedCrawlUrl) {
							if (queuedCrawlUrl[0] == tCrawlUrl) {
								queuedCrawlUrl[0] = null;
							}
						}

						queueNextScrape();
						try {
							Boot.getLogger().log("Scraping: " + tCrawlUrl, true);
							StoredProcedures.addScrape(session, tCrawlUrl, crawlContent);
						} catch (InterruptedException e) {
							Boot.getLogger().log(e, "While adding the scrape to the database "
									+ "the thread was interrupted. \n" 
									+ "\t tCrawlUrl is " + tCrawlUrl);
						}
					}
				}))) {

				queuedCrawlUrl[0] = null;
				return true;
			}
			return false;
		}
	}
}