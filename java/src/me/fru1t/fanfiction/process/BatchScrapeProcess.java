package me.fru1t.fanfiction.process;

import java.util.HashMap;

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
public class BatchScrapeProcess implements Runnable {
	private static final int WATCHDOG_SLEEP_TIME_MS = 500;
	private static final int batchSize = 1000;

	private ConcurrentProducer<String> urlProducer;
	private Session session;
	private String[] queuedCrawlUrl;
	private HashMap<String, String> batchCrawlContent;

	/**
	 * Creates a new scrape process given the urls in the form of a producer and the session name.
	 * @param urlProducer
	 * @throws InterruptedException 
	 */
	public BatchScrapeProcess(ConcurrentProducer<String> urlProducer, SessionName session) throws InterruptedException {
		this.urlProducer = urlProducer;
		this.session = new Session(session);
		this.queuedCrawlUrl = new String[1];
		this.queuedCrawlUrl[0] = null;
		this.batchCrawlContent = new HashMap<>();
	}

	@Override
	public void run() {
		Boot.getLogger().log("Running ScrapeProcess with session name: " + session.getName() 
								+ "\n\t with ID number " + session.getID() 
								+ "\n\t on DB " +  Boot.database
								+ "\n\t from ID " + Boot.startid + " to " + Boot.endid, true);

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
						
						HashMap<String, String> temp = null;
						synchronized(batchCrawlContent) {
							batchCrawlContent.put(tCrawlUrl, crawlContent);
							if (batchCrawlContent.size() > batchSize 
									|| (urlProducer.isComplete() && batchCrawlContent.size() > 0)) {
								Boot.getLogger().log("Batching " + batchCrawlContent.size() 
														+ " inserts until " + tCrawlUrl, true);
								temp = new HashMap<String, String>();
								temp.putAll(batchCrawlContent);
								batchCrawlContent.clear();
							}
						}
						
						try {
							if (temp != null) StoredProcedures.addScrapeBatch(session, temp);
						} catch (InterruptedException e) {
							Boot.getLogger().log(e, "While adding the scrape to the database "
									+ "the thread was interrupted.");
						}
					}
				})))
			{
				queuedCrawlUrl[0] = null;
				return true;
			}
			
			return false;
		}
	}
}
