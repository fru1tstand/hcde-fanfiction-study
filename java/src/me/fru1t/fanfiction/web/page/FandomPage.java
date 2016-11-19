package me.fru1t.fanfiction.web.page;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.FandomStoryListElement;
import me.fru1t.web.Page;


public class FandomPage extends Page {
	private static final String RESULT_ELEMENTS_SELECTOR =
			"#content_wrapper_inner .z-list.zhover.zpointer";
	private static final String FANDOM_PREV_ELEMENT_SELECTOR =
			".xcontrast_txt.icon-chevron-right.xicon-section-arrow";
	private static final String PAGE_NUMBERS_SELECTOR = "div#content_wrapper_inner center a";

	// Group 1: Page count
	private static final Pattern PAGE_NUMBERS_HREF_PATTERN = Pattern.compile("^.+&p=(\\d+)$");
	private static final int PAGE_COUNT_GROUP = 1;

	private List<FandomStoryListElement> resultElements;
	private int maxPages;
	private String fandom;

	public FandomPage(String document) {
		this(Jsoup.parse(document));
	}

	public FandomPage(Document document) {
		super(document);
		this.resultElements = null;
		this.maxPages = -1;
	}

	public List<FandomStoryListElement> getStoryElements() {
		if (resultElements == null) {
			this.resultElements = new ArrayList<>();
			try {
				this.fandom = document
					.select(FANDOM_PREV_ELEMENT_SELECTOR)
					.get(0)
					.nextSibling()
					.toString();
			} catch (IndexOutOfBoundsException e) {
				Boot.getLogger().log(e, "Couldn't find the book name from this search page");
			}
			for (Element element : document.select(RESULT_ELEMENTS_SELECTOR)) {
				resultElements.add(new FandomStoryListElement(this.fandom, element));
			}
		}
		return resultElements;
	}

	public int getMaxPages() {
		if (maxPages == -1) {
			// Check if the page has any story elements, if it doesn't, this fandom doesn't exist
			// anymore.
			if (this.getStoryElements().size() < 1) {
				maxPages = 0;
				return 0;
			}

			// Goes through all elements containing links to other pages of this fandom. Finds the
			// highest value "Last >" button
			Elements pageNumberLinks = document.select(PAGE_NUMBERS_SELECTOR);
			int foundMaxPages = 1;
			for (Element pageNumberLink : pageNumberLinks) {
				Matcher pageNumberValueMatcher =
						PAGE_NUMBERS_HREF_PATTERN.matcher(pageNumberLink.attr(LINK_HREF_ATTR));
				if (!pageNumberValueMatcher.matches()) {
					Boot.getLogger().log("The link " + pageNumberLink.outerHtml()
						+ " wound up in the page number links pile. Ignoring for now, but this may"
						+ "be indicitive of a problem.", true);
					continue;
				}
				int pageNumberValue =
						Integer.parseInt(pageNumberValueMatcher.group(PAGE_COUNT_GROUP));
				foundMaxPages = (pageNumberValue > foundMaxPages) ? pageNumberValue : foundMaxPages;
			}
			maxPages = foundMaxPages;
		}

		return maxPages;
	}
}
