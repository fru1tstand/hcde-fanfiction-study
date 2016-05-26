package me.fru1t.fanfiction.process.scrape;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.util.concurrent.GenericProducer;

/**
 * Defines the production of URLs for fandom pages. Fandom pages contain a list of stories with
 * metadata.
 * (eg https://www.fanfiction.net/anime/Naruto/?&srt=1&r=103&p=3)
 */
public class FandomPage extends GenericProducer<String> {
	public static final String SESSION_NAME = "5-26 Test Scrape";
	private static final String CRAWL_URL = "https://www.fanfiction.net/book/Harry-Potter/?&srt=2&lan=1&r=4&p=";
	private static final int MAX_PAGES = 4447;

	private int currentPage;

	public FandomPage() {
		currentPage = 0;
	}

	@Override
	public @Nullable String take() {
		if (currentPage++ > MAX_PAGES) {
			return null;
		}
		return CRAWL_URL + currentPage;
	}

}
