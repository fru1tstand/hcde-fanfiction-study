package me.fru1t.fanfiction.process.scrape;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.producers.FandomProducer.Fandom;
import me.fru1t.fanfiction.web.page.FandomPage;
import me.fru1t.util.concurrent.GenericProducer;

/**
 * Defines the production of URLs for fandom pages. Fandom pages contain a list of stories with
 * metadata.
 * (eg https://www.fanfiction.net/anime/Naruto/?&srt=1&r=103&p=3)
 */
public class FandomPageUrlProducer extends GenericProducer<String> {
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
	private static final String PAGE_NUMBERS_SELECTOR = "div#content_wrapper_inner center a";
	// Matches for page parameter
	private static final Pattern PAGE_NUMBERS_HREF_PATTERN = Pattern.compile("^.+&p=(\\d+)$");
	private static final int PAGE_COUNT_GROUP = 1;
	private static final String LINK_HREF_ATTR = "href";
	private static final int MAX_RETRIES = 5;


	private GenericProducer<Fandom> fandomProducer;
	private Fandom currentFandom;
	private int currentPage;
	private int maxPages;

	public FandomPageUrlProducer(Fandom fandom) {
		this(new GenericProducer<Fandom>() {
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

	public FandomPageUrlProducer(GenericProducer<Fandom> fandomProducer) {
		this.currentFandom = null;
		this.currentPage = -1;
		this.maxPages = -1;
		this.fandomProducer = fandomProducer;
		nextFandom();
	}

	@Override
	public synchronized @Nullable String take() {
		// Check if there are any more pages + 1 for good measure
		if (currentPage > maxPages + 1) {
			if (!nextFandom()) {
				return null;
			}
		}
		return getFandomUrl(currentFandom, currentPage++);
	}

	/**
	 * Mutates maxPages, currentFandom, and currentPage while incrementing fandomProducer.
	 * Leaves currentPage alone.
	 */
	private boolean nextFandom() {
		// Fetch next fandom, end condition being if there are no more fandoms.
		currentFandom = fandomProducer.take();
		currentPage = 1;
		if (currentFandom == null) {
			return false;
		}

		// Get fandom max pages.
		FandomPage fp = null;
		int foundMaxPages = -1;
		int retries = 0;
		do {
			// Get fandom content
			String fandomPageString = "";
			try {
				fandomPageString = Boot.getCrawler().getContents(getFandomUrl(currentFandom, 1));
			} catch (InterruptedException e) {
				Boot.getLogger().log(e, "Interrupted when fetching page details in scrape/FandomPage");
				return false;
			}

			// Look for max pages
			Document fandomPageDoc = Jsoup.parse(fandomPageString);
			Elements pageNumberLinks = fandomPageDoc.select(PAGE_NUMBERS_SELECTOR);
			for (Element pageNumberLink : pageNumberLinks) {
				Matcher m = PAGE_NUMBERS_HREF_PATTERN.matcher(pageNumberLink.attr(LINK_HREF_ATTR));
				if (!m.matches()) {
					Boot.getLogger().log("The link " + pageNumberLink.outerHtml()
						+ " wound up in the page number links pile. Ignoring for now, but this may"
						+ "be indicitive of a problem.");
					continue;
				}
				int thisPageNumber = Integer.parseInt(m.group(PAGE_COUNT_GROUP));
				if (thisPageNumber > foundMaxPages) {
					foundMaxPages = thisPageNumber;
				}
			}
			if (foundMaxPages < 0) {
				Boot.getLogger().log("No page number links found when starting to scrape fandom "
						+ currentFandom.toString() + ". Retrying.");
			}
			if (Boot.DEBUG && foundMaxPages > 0) {
				Boot.getLogger().debug("Found " + foundMaxPages + " pages, but setting to 1 for"
						+ "debug purposes", this.getClass());
				foundMaxPages = 1;
			}

			// The page loaded correctly if there are book result elements, so if we still haven't
			// found a max pages, it means there is only 1 page of results.
			fp = new FandomPage(fandomPageDoc);
			if (foundMaxPages < 1 && fp.getStoryElements().size() > 0) {
				foundMaxPages = 1;
			}

			// While no max pages found, no stories found, and within retry limits.
		} while ((foundMaxPages == -1)
				&& (fp == null || fp.getStoryElements().size() < 1)
				&& (retries++ < MAX_RETRIES));

		// Still unable to find the max pages.
		if (foundMaxPages < 0) {
			Boot.getLogger().log("Couldn't find page number links AND no book result elements"
					+ " returned after " + MAX_RETRIES + " attempts. Skipping this fandom.");
			return nextFandom(); // Recursive next
		}

		// Found it, lets go
		Boot.getLogger().log("Found " + foundMaxPages + " pages to scrape for "
				+ currentFandom.toString());
		maxPages = foundMaxPages;
		return true;
	}

	private String getFandomUrl(Fandom fandom, int page) {
		return BASE_URL + fandom.url + ANY_RATING_ANY_LANGUAGE_SORTED_PUBLISH + PAGE_PARAM + page;
	}
}
