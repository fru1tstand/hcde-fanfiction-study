package me.fru1t.fanfiction.process.convert;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.ReviewProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.util.Consumer;
import me.fru1t.fanfiction.web.page.ReviewListPage;
import me.fru1t.fanfiction.web.page.element.ReviewElement;

/**
 * This class defines a consumer which eats profile pages,
 * extract user information (e.g. age) and store it within the database.
 */
@Deprecated
public class ReviewListToReviews extends Consumer<Scrape> {
	
	/**
	 * https://www.fanfiction.net/r/[ff_story_id]/[chapter]/[page_number]/;
	 * if [chapter] is 0, then all chapters' reviews are shown
	 * 
	 * `review` table's columns: 
	 * id, ff_story_id, which_chapter, date, reviewer_id
	 */
	
	// Matches profile URLs with or without filters.
	private static final Pattern REVIEW_LIST_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/r/(?<ffStoryId>[0-9]+)/(?<chapter>[0-9]+)/([0-9]+)/$");

	@Override
	public void eat(Scrape scrape) {
		//long startTime = (new Date()).getTime();
		
		// Check for scrape URL validity.
		Matcher m = REVIEW_LIST_URL_PATTERN.matcher(scrape.url);
		if (!m.matches()) {
			Boot.getLogger().log("Invalid URL for Profile, ignoring: " + scrape.url, true);
			return;
		}
		
		try {
			Document reviewListPageDoc = Jsoup.parse(scrape.content);
			
			// parse for ff_story_id and chapter that cannot be null
			m = REVIEW_LIST_URL_PATTERN.matcher(scrape.url);
			if (m.matches()) {
				int ffStoryId = Integer.parseInt(m.group("ffStoryId"));
				int chapter = Integer.parseInt(m.group("chapter"));
				
				List<ReviewElement> list = (new ReviewListPage(ffStoryId, chapter, reviewListPageDoc, -1)).getReviewElements();
				ReviewProcedures.addReviewAndReviewerBatch(list);
			} else {
				throw new Exception("Could not extract ffStoryId and/or chapter in url: " + scrape.url);
			}
			
		} catch (NumberFormatException e) {
			Boot.getLogger().log(e, "Invalid ffStoryId and/or chapter in url " + scrape.url + " with details: ");
		} catch (Exception e) {
			Boot.getLogger().log(e, "Skipped scrape with ID " + scrape.id + " due to:");
		}
		
		
	}
}
