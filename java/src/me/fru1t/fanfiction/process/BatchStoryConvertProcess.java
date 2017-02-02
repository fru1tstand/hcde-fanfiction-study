package me.fru1t.fanfiction.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.StoryContentProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.web.page.element.StoryContentElement;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Converts rows from a table in the database into something else.
 */
public class BatchStoryConvertProcess<T extends DatabaseProducer.Row<?>> implements Runnable {
	private static int BUFFER_SIZE = 1 * 1024 * 1024;
	private ScrapeProducer producer;
	private int scrapedLen;

	// Matches profile URLs with or without filters.
	private static final Pattern STORY_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/s/(?<ffStoryId>[0-9]+)/(?<chapter>[0-9]+)/$");

	ArrayList<StoryContentElement> storyElements;
	

	public BatchStoryConvertProcess(ScrapeProducer producer) throws InterruptedException {
		this.producer = producer;
		this.storyElements = new ArrayList<>(); 
		this.scrapedLen = 0;
	}

	private void batchInsert() throws InterruptedException {
		long startTime = (new Date()).getTime();
		
		StoryContentProcedures.addStoryContents(storyElements);
		
		Boot.getLogger().log("Processed story content for /" + storyElements.size()
								+ "; Took: " + ((new Date()).getTime() - startTime) + "ms", true);
		storyElements.clear();
		scrapedLen = 0;
	}
	
	@Override
	public void run() {
        Boot.getLogger().log("Running BatchStoryConvertProcess with : "
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

				StoryContentElement se = scrapeToStoryContentElement(scrape);
				if (se != null) {
					se.setScrapeId(scrape.id);
					storyElements.add(se);
					scrapedLen += se.getContentLen();
				}
				
				scrape = producer.take();
			}

			// flush
			if (storyElements.size() > 0) batchInsert();
		} catch (InterruptedException e) {
			Boot.getLogger().log("InterruptedException. Skipped scrape id " + scrape.id + "; URL " + scrape.url, true);
			System.exit(42);
		} catch (Exception e) {
			Boot.getLogger().log("Some Other Exception. Skipped scrape id " + scrape.id + "; URL " + scrape.url, true);
			System.exit(42);
		}
		
		Boot.getLogger().log("Finished batchConvertProcess with session name: " + Boot.getSessionOfThisRun().getName(), true);
	}

	private StoryContentElement scrapeToStoryContentElement(Scrape scrape) throws Exception {
		Boot.getLogger().log("Covert scrape id " + scrape.id +  "; URL " + scrape.url, false);
		
		// Check for scrape URL validity.
		Matcher m = STORY_URL_PATTERN.matcher(scrape.url);
		if (m.matches()) {
			Document storyPageDoc = Jsoup.parse(scrape.content);
			int ffStoryId = Integer.parseInt(m.group("ffStoryId"));
			int chapter = Integer.parseInt(m.group("chapter"));
			return new StoryContentElement(storyPageDoc, ffStoryId, chapter);
		}
		
		throw new Exception("Scrape id " + scrape.id + "; URL " + scrape.url + " did not match STORY_URL_PATTERN");
	}

}
