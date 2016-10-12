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
	private static final int MAX_CONCURRENT_REQUESTS_OUT = 1;
	private static final int WATCHDOG_SLEEP_TIME_MS = 5000;

	private ConcurrentProducer<String> urlProducer;
	private Session session;
	private boolean isFinished;
	private int concurrentRequests;
	private boolean isInterrupted;

	/**
	 * Creates a new scrape process given the urls in the form of a producer and the session name.
	 * @param urlProducer
	 */
	public ScrapeProcess(ConcurrentProducer<String> urlProducer, Session session) {
		this.urlProducer = urlProducer;
		this.session = session;
		this.isFinished = false;
		this.isInterrupted = false;
		this.concurrentRequests = 0;
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ScrapeProcess with session name: " + session);

		// Startup loop, saturate queue by calling until it returns false.
		while (queueNextScrape());

		// Watchdog in case of thread death.
		while (!isFinished && !isInterrupted) {
			if (Thread.currentThread().isInterrupted()) {
				Boot.getLogger().log("ScrapeProcess with session name: " + session
						+ " was interrupted and is now quitting.");
				isInterrupted = true;
				break;
			}

			if (concurrentRequests < MAX_CONCURRENT_REQUESTS_OUT) {
				Boot.getLogger().log("[watchdog] Found the queue wasn't full: "
						+ concurrentRequests + " queued of " + MAX_CONCURRENT_REQUESTS_OUT
						+ " maximum. Filling...");
				while (queueNextScrape());
			}

			try {
				ThreadUtils.waitGauss(WATCHDOG_SLEEP_TIME_MS);
			} catch (InterruptedException e) {
				isInterrupted = true;
				Boot.getLogger().log("Watchdog was interrupted and is shutting down all children");
				return;
			}
		}

		Boot.getLogger().log("Completed ScrapeProcess with session name: " + session);
	}

	private boolean queueNextScrape() {
		if (isInterrupted) {
			return false;
		}

		if (concurrentRequests >= MAX_CONCURRENT_REQUESTS_OUT) {
			return false;
		}

		if (urlProducer.isBlocked()) {
			Boot.getLogger().log("The URL Producer is blocked.");
			return false;
		}

		String crawlUrl = urlProducer.take();
		if (crawlUrl == null) {
			isFinished = true;
			return false;
		}

		concurrentRequests++;
		Boot.getCrawler().sendRequest(new Request(crawlUrl, new Consumer<String>() {
			@Override
			public void eat(String crawlContent) {
				concurrentRequests--;
				queueNextScrape();
				try {
					StoredProcedures.addScrape(session, crawlUrl, crawlContent);
				} catch (InterruptedException e) {
					Boot.getLogger().log(e, "While adding the scrape to the database the thread "
							+ "was interrupted.");
				}
			}
		}));
		return true;
	}
}
