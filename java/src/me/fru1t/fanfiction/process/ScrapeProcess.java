package me.fru1t.fanfiction.process;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.Session;
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
	private String queuedCrawlUrl;

	/**
	 * Creates a new scrape process given the urls in the form of a producer and the session name.
	 * @param urlProducer
	 */
	public ScrapeProcess(ConcurrentProducer<String> urlProducer, Session session) {
		this.urlProducer = urlProducer;
		this.session = session;
		this.queuedCrawlUrl = null;
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ScrapeProcess with session name: " + session);

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
				Boot.getLogger().log("Watchdog was interrupted and is shutting down all children");
				return;
			}
		}

		Boot.getLogger().log("Completed ScrapeProcess with session name: " + session);
	}

	private boolean queueNextScrape() {
		if (queuedCrawlUrl == null) {
			queuedCrawlUrl = urlProducer.take();
		}
		if (queuedCrawlUrl == null) {
			return false;
		}

		String tCrawlUrl = queuedCrawlUrl;
		if (Boot.getCrawler().sendRequest(new Request(tCrawlUrl, new Consumer<String>() {
				@Override
				public void eat(String crawlContent) {
					// If the crawler hits cache, this process is not async. Thus we need to check
					// that queuedCrawlUrl == tCrawlUrl
					if (queuedCrawlUrl == tCrawlUrl) {
						queuedCrawlUrl = null;
					}

					queueNextScrape();
					try {
						StoredProcedures.addScrape(session, tCrawlUrl, crawlContent);
					} catch (InterruptedException e) {
						Boot.getLogger().log(e, "While adding the scrape to the database "
								+ "the thread was interrupted.");
					}
				}
			}))) {
			queuedCrawlUrl = null;
			return true;
		}
		return false;
	}
}
