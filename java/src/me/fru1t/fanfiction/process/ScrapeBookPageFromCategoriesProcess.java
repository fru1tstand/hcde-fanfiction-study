package me.fru1t.fanfiction.process;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoredProcedures;
import me.fru1t.fanfiction.web.page.BookSearchPage;
import me.fru1t.fanfiction.web.page.CategoryPage;
import me.fru1t.web.MultiIPCrawler;

public class ScrapeBookPageFromCategoriesProcess implements Runnable {
	private static final String SESSION_NAME = "Top 100 stories from top 15 fandoms from all categories, May 12, 2016";
	private static final byte[][] ips = {
			{ (byte) 104, (byte) 128, (byte) 237, (byte) 128 },
			{ (byte) 104, (byte) 128, (byte) 233, (byte) 73 },
			{ (byte) 45, (byte) 58, (byte) 54, (byte) 250 }
	};
	private static final double AVG_SLEEP_TIME_PER_IP = 7.5;
	private static final double STDEV = 1.0;
	
	private static final int FANDOMS_TO_FETCH_PER_CATEGORY = 15;
	private static final int STORIES_TO_FETCH_PER_FANDOM = 100;
	private static final String LINK_ATTRIBUTE = "href";
	
	//https://www.fanfiction.net/anime/Beyblade/
	private static final Pattern STORY_URL_PATTEN =
			Pattern.compile("^https://www.fanfiction.net/([^/]+)/([^/]+)/$");
	
	private static final String CATEGORY_CRAWL_URL = "https://www.fanfiction.net/%s";
	private static final String FANFICTION_BASE_URL = "https://www.fanfiction.net";
	private static final String[] CATEGORIES =
		{ "anime", "book", "cartoon", "comic","game", "play", "movie", "tv" };
	
	private static final String STORY_LIST_PARAMETERS = "?&srt=3&lan=1&r=103&p=";

	@Override
	public void run() {
		Boot.getLogger()
			.log("Running ScrapeBookPageFromCategoriesProcess with Session Name: " + SESSION_NAME);
		
		ArrayList<String> fandomUrls = new ArrayList<>();
		MultiIPCrawler crawler = new MultiIPCrawler(Boot.getLogger(), ips);
		int waitTime = 0;
		Random rand = new Random();
		
		// Get fandom urls
		for (String category : CATEGORIES) {
			String categoryPageUrl = String.format(CATEGORY_CRAWL_URL, category);
			Boot.getLogger().log("Scraping " + categoryPageUrl);
			String categoryPageScrape = crawler.getContents(categoryPageUrl);
			StoredProcedures.addScrape(SESSION_NAME + " - category", categoryPageUrl, categoryPageScrape);
			CategoryPage catPage = new CategoryPage(categoryPageScrape);
			int i = 0;
			for (Element fandomLinkEl : catPage.getFandomLinks()) {
				fandomUrls.add(fandomLinkEl.attr(LINK_ATTRIBUTE));
				if (i++ > FANDOMS_TO_FETCH_PER_CATEGORY) {
					break;
				}
			}
			try {
				waitTime = Math.max(0, 
						(int) ((rand.nextGaussian() * STDEV 
								+ (AVG_SLEEP_TIME_PER_IP / ips.length)) 
								* 1000));
				Thread.sleep(waitTime);
			} catch (InterruptedException e) {
				Boot.getLogger().log(e);
				return;
			}
		}
		
		Boot.getLogger().log("Found " + fandomUrls.size() + " fandoms to scrape.");

		// Fetch page and scrape story
		int pagesScraped = 0;
		for (String fandomUrl : fandomUrls) {
			for (int page = 1; page < 6; page++) {
				String listUrl = FANFICTION_BASE_URL + fandomUrl + STORY_LIST_PARAMETERS + page;
				Boot.getLogger().log("Scraping " + listUrl);
				String listScrape = crawler.getContents(listUrl);
				StoredProcedures.addScrape(SESSION_NAME + " - booklist", listUrl, listScrape);
				pagesScraped++;
				
				try {
					waitTime = Math.max(0, 
							(int) ((rand.nextGaussian() * STDEV 
									+ (AVG_SLEEP_TIME_PER_IP / ips.length)) 
									* 1000));
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					Boot.getLogger().log(e);
					return;
				}
			}
		}
		
		Boot.getLogger().log("Scraped " + pagesScraped + " book list pages.");
	}
	
	
}
