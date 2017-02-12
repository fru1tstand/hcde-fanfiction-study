package me.fru1t.fanfiction.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.ReviewProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.web.page.ReviewListPage;
import me.fru1t.fanfiction.web.page.element.ReviewElement;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Converts rows from a table in the database into something else.
 */
public class BatchReviewConvertProcess<T extends DatabaseProducer.Row<?>> implements Runnable {

	private static int BUFFER_SIZE = 1 * 1024 * 1024;
	private ScrapeProducer producer;
	private int scrapedLen;

	// Matches profile URLs with or without filters.
	private static final Pattern REVIEW_LIST_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/r/(?<ffStoryId>[0-9]+)/([0-9]+)/([0-9]+)/$");

	ArrayList<ReviewElement> reviewElements;

	public BatchReviewConvertProcess(ScrapeProducer producer) throws InterruptedException {
		this.producer = producer;
		this.reviewElements = new ArrayList<>(); 
		this.scrapedLen = 0;
	}

	private void batchInsert() throws InterruptedException {
		long startTime = (new Date()).getTime();
		ReviewProcedures.addReviewReviewer(reviewElements);
		Boot.getLogger().log("Processed addReviewAndReviewer for /" + reviewElements.size()
								+ "; Took: " + ((new Date()).getTime() - startTime) + "ms", true);
		reviewElements.clear();
		scrapedLen = 0;
	}
	
	@Override
	public void run() {
        Boot.getLogger().log("Running BatchReviewConvertProcess with : "
        		+ "\n\t\t server_name  		: " + Boot.getServerName() 
        		+ "\n\t\t command      		: " + Boot.getCommand()
        		+ "\n\t\t scrape_tablename 	: " + Boot.getScrapeTablename()
        		+ "\n\t\t from_session     	: " + producer.getScrapingSessionNames()
        		+ "\n\t\t my_session_name  	: " + Boot.getSessionOfThisRun().getName(), true);

		@Nullable Scrape scrape = producer.take();
		try {
			while (scrape != null) {
				if (scrapedLen > BUFFER_SIZE) {
					batchInsert();
				}
				
				ReviewListPage pageSummary = scrapeToReviewListPage(scrape);
				reviewElements.addAll(pageSummary.getReviewElements());
				scrapedLen += pageSummary.getContentLen();
				scrape = producer.take();
			}

			// flush
			if (reviewElements.size() > 0) batchInsert();
		} catch (InterruptedException e) {
			Boot.getLogger().log(e, "InterruptedException. Skipped scrape id " + scrape.id + "; URL " + scrape.url);
			System.exit(42);
		} catch (Exception e) {
			Boot.getLogger().log(e, "Some Other Exception. Skipped scrape id " + scrape.id + "; URL " + scrape.url);
			System.exit(42);
		}
		
		Boot.getLogger().log("Finished BatchReviewConvertProcess with session name: " 
					+ Boot.getSessionOfThisRun().getName(), true);
	}

	private ReviewListPage scrapeToReviewListPage(Scrape scrape) throws Exception {
		Boot.getLogger().log("Convert scrape id " + scrape.id +  ", URL " + scrape.url, false);

		// Check for scrape URL validity.
		Matcher m = REVIEW_LIST_URL_PATTERN.matcher(scrape.url);
		if (m.matches()) {
			Document reviewListPageDoc = Jsoup.parse(scrape.content);
			int ffStoryId = Integer.parseInt(m.group("ffStoryId"));

			return new ReviewListPage(ffStoryId, reviewListPageDoc, scrape.id);
		}
		
		throw new Exception("Scrape id " + scrape.id + "; URL " + scrape.url + " did not match REVIEW_LIST_URL_PATTERN");
	}

}
