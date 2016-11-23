package me.fru1t.fanfiction.process;

/*
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.ReviewProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.web.page.ReviewListPage;
import me.fru1t.fanfiction.web.page.element.ReviewElement;
import me.fru1t.util.concurrent.DatabaseProducer;
*/

/**
 * Converts rows from a table in the database into something else.
 */
public class BatchNewThreadReviewConvertProcess implements Runnable {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
/*
	private static int batchSize = 2000;
	private DatabaseProducer<Scrape, Integer> producer;
	private SessionName convertSession;

	// Matches profile URLs with or without filters.
	private static final Pattern REVIEW_LIST_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/r/(?<ffStoryId>[0-9]+)/(?<chapter>[0-9]+)/([0-9]+)/$");

	ArrayList<ReviewElement> reviewElements;

	public BatchNewThreadReviewConvertProcess(DatabaseProducer<Scrape, Integer> producer, 
			SessionName convertProfilePages161110) {
		this.producer = producer;
		this.convertSession = convertProfilePages161110;
		this.reviewElements = new ArrayList<>(); 
	}

	private synchronized ArrayList<ReviewElement> deepCopy() {
		// create a deep copy and empty the original
		ArrayList<ReviewElement> result = new ArrayList<ReviewElement>();
		synchronized(reviewElements) {
			for (ReviewElement re : reviewElements) {
				result.add(re);
			}
			reviewElements.clear();
		}
		return result;
	}
	
	private void batchInsert(ArrayList<ReviewElement> deepCopiedElements) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				long startTime = (new Date()).getTime();
				try {
					ReviewProcedures.processAddReviewBatch(deepCopiedElements);
					Boot.getLogger().log("Processed processUserScrapeToProfileBatch for /" + reviewElements.size()
					+ "; Took: " + ((new Date()).getTime() - startTime) + "ms", true);
				} catch (InterruptedException e) {
					
				}
			}
		}).run();
	}
	
	@Override
	public void run() {
		Boot.getLogger().log("Running batchConvertProcess with session name: " + convertSession, true);

		@Nullable Scrape scrape = producer.take();
		while (scrape != null) {
			try {
				synchronized(reviewElements) {
					reviewElements.addAll(scrape2reviewElements(scrape));
				}
			} catch (Exception e) {
				Boot.getLogger().log("Trouble with scrape2profileElement. " 
						+ "Skipped scrape id " + scrape.id + ", URL " + scrape.url, true);
			}

			if (reviewElements.size() > batchSize) batchInsert(deepCopy());
			scrape = producer.take();
		}

		// flush
		if (reviewElements.size() > 0) batchInsert(deepCopy());
		Boot.getLogger().log("Finished batchConvertProcess with session name: " + convertSession, true);
	}

	private ArrayList<ReviewElement> scrape2reviewElements(Scrape scrape) throws Exception {
		//Boot.getLogger().log("Convert scrape id " + scrape.id +  ", URL " + scrape.url, false);

		// Check for scrape URL validity.
		Matcher m = REVIEW_LIST_URL_PATTERN.matcher(scrape.url);
		if (m.matches()) {
			Document reviewListPageDoc = Jsoup.parse(scrape.content);
			int ffStoryId = Integer.parseInt(m.group("ffStoryId"));
			int chapter = Integer.parseInt(m.group("chapter"));

			return (new ReviewListPage(ffStoryId, chapter, reviewListPageDoc, scrape.id)).getReviewElements();
		} else {
			throw new Exception("Scrape id " + scrape.id + ", URL " + scrape.url 
								+ " did not match REVIEW_LIST_URL_PATTERN");
		}
	}
*/
}
