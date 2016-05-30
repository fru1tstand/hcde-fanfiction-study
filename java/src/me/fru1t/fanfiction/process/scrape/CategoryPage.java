package me.fru1t.fanfiction.process.scrape;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.util.concurrent.GenericProducer;

public class CategoryPage extends GenericProducer<String> {
	private static final String[] CATEGORIES =
		{ "anime", "book", "cartoon", "comic", "game", "misc", "play", "movie", "tv" };
	private static final String FORMAT_URL = "https://www.fanfiction.net/%s/";

	private int categoryIndex;

	public CategoryPage() {
		categoryIndex = 0;
	}

	@Override
	public @Nullable String take() {
		if (categoryIndex > CATEGORIES.length) {
			return null;
		}
		return String.format(FORMAT_URL, CATEGORIES[categoryIndex++]);
	}

}
