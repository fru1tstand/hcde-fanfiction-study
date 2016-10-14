package me.fru1t.fanfiction.process.scrape;

import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.producers.FandomProducer.Fandom;
import me.fru1t.fanfiction.web.page.FandomPage;
import me.fru1t.util.Consumer;
import me.fru1t.util.Producer;
import me.fru1t.util.concurrent.ConcurrentProducer;
import me.fru1t.web.Request;

/**
 * Defines the production of URLs for fandom pages. Fandom pages contain a list of stories with
 * metadata.
 * (eg https://www.fanfiction.net/anime/Naruto/?&srt=1&r=103&p=3)
 */
public class FandomPageUrlProducer extends ConcurrentProducer<String> {
	/**
	 * Format parameters:
	 * 1 Category
	 * 2 Fandom
	 * 3 Parameters
	 */
	private static final String BASE_URL = "https://www.fanfiction.net";
	private static final String ANY_RATING_ANY_LANGUAGE_SORTED_PUBLISH = "?&srt=2&r=10";
	private static final String PAGE_PARAM = "&p=";

	// Warning: Selects more than 1 element. One must parse through them all.
	// Matches for page parameter
	private static final int EXTRA_PAGE_MIN_STORIES_THRESHOLD = 5000;

	private static final long WAIT_BEFORE_RETRY_SCRAPE_MS = 1000;

	private static boolean isComplete;
	private static boolean isWaitingForFirstPage;

	private Producer<Fandom> fandomProducer;
	private Fandom currentFandom;
	private int currentPage;
	private int maxPages;

	public FandomPageUrlProducer(Fandom fandom) {
		this(new Producer<Fandom>() {
			boolean hasTaken = false;
			@Override
			public @Nullable Fandom take() {
				if (hasTaken) {
					return null;
				}
				hasTaken = true;
				return fandom;
			}

		});
	}

	public FandomPageUrlProducer(Producer<Fandom> fandomProducer) {
		this.currentFandom = null;
		this.currentPage = -1;
		this.maxPages = -1;
		this.fandomProducer = fandomProducer;
		nextFandom();
	}

	/**
	 * Gets the next page to scrape, or null if we're waiting or complete. Returns immediately with
	 * no blocking.
	 */
	@Override
	public synchronized @Nullable String take() {
		// Precheck
		if (isComplete || isWaitingForFirstPage) {
			return null;
		}

		// Check if we still have pages ready to serve
		if (currentPage <= maxPages + ((maxPages > EXTRA_PAGE_MIN_STORIES_THRESHOLD) ? 1 : 0)) {
			return getFandomUrl(currentFandom, currentPage++);
		}

		// We need to get a new fandom
		nextFandom();
		return null;
	}

	// Async gets the next fandom. Returns immediately.
	private void nextFandom() {
		// Get the next fandom object
		isWaitingForFirstPage = true;
		currentFandom = fandomProducer.take();
		currentPage = 1;

		// No more fandoms from the database, so we're done.
		if (currentFandom == null) {
			isComplete = true;
			return;
		}

		// Async fetch fandom page 1 and get max pages.
		getNextFandom();
	}

	private void getNextFandom() {
		if (!Boot.getCrawler().sendRequest(new Request(
				getFandomUrl(currentFandom, 1),
				new Consumer<String>() {
					@Override
					public void eat(String food) {
						FandomPage fp = new FandomPage(Jsoup.parse(food));

						// Check if we found pages
						if (fp.getMaxPages() < 1) {
							// Send a message that we didn't, and try the next fandom
							Boot.getLogger().log("No pages found for fandom '" + currentFandom.name
									+ "' at '" + getFandomUrl(currentFandom, 1) + "'");
							nextFandom();
						} else {
							// Otherwise, we're good to go
							maxPages = fp.getMaxPages();
							Boot.getLogger().log("Found " + maxPages + " pages for "
									+ currentFandom.toString());
							isWaitingForFirstPage = false;
						}
					}
				}
		))) {
			getNextFandom();
			try {
				Thread.sleep(WAIT_BEFORE_RETRY_SCRAPE_MS);
			} catch (InterruptedException e) {
				Boot.getLogger().log(e);
			}
		}
	}

	private String getFandomUrl(Fandom fandom, int page) {
		return BASE_URL + fandom.url + ANY_RATING_ANY_LANGUAGE_SORTED_PUBLISH + PAGE_PARAM + page;
	}

	@Override
	public boolean isComplete() {
		return isComplete;
	}
}
