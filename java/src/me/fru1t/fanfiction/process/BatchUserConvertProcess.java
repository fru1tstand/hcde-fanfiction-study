package me.fru1t.fanfiction.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.UserToProfileProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.web.page.element.ProfileElement;
import me.fru1t.util.concurrent.DatabaseProducer;

/**
 * Converts rows from a table in the database into something else.
 */
public class BatchUserConvertProcess<T extends DatabaseProducer.Row<?>> implements Runnable {

	private static int batchSize = 2000;
	private ScrapeProducer producer;

	// Matches profile URLs with or without filters.
	private static final Pattern USER_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/u/(?<ffId>[0-9]+)$");

	ArrayList<ProfileElement> profileElements;

	public BatchUserConvertProcess(ScrapeProducer producer) throws InterruptedException {
		this.producer = producer;
		this.profileElements = new ArrayList<>(); 
		
	}

	private void batchInsert() throws InterruptedException{
		long startTime = (new Date()).getTime();
		UserToProfileProcedures.addUserProfileBatch(profileElements);
		Boot.getLogger().log("Processed processUserScrapeToProfileBatch for /" + profileElements.size()
								+ "; Took: " + ((new Date()).getTime() - startTime) + "ms", true);
		profileElements.clear();
	}
	
	@Override
	public void run() {
        Boot.getLogger().log("Running BatchUserConvertProcess with : "
        		+ "\n\t\t server_name  		: " + Boot.getServerName() 
        		+ "\n\t\t command      		: " + Boot.getCommand()
        		+ "\n\t\t scrape_tablename 	: " + Boot.getScrapeTablename()
        		+ "\n\t\t from_session     	: " + producer.getScrapingSessionNames()
        		+ "\n\t\t my_session_name  	: " + Boot.getSessionOfThisRun().getName(), true);

		@Nullable Scrape scrape = producer.take();
		try {
			while (scrape != null) {
				try {
					ProfileElement pe = scrapeToProfileElement(scrape);
					if (pe != null) pe.setScrapeId(scrape.id);
					profileElements.add(pe);
				} catch (Exception e) {
					Boot.getLogger().log("Trouble with scrape2profileElement. " 
										  + "Skipped scrape id " + scrape.id + ", URL " + scrape.url, true);
				}
				
				if (profileElements.size() > batchSize) batchInsert();
				scrape = producer.take();
			}

			// flush
			if (profileElements.size() > 0) batchInsert();
		} catch (InterruptedException e) {
			Boot.getLogger().log("InterruptedException. Skipped scrape id " + scrape.id 
								  + ", URL " + scrape.url, true);
		}
		
		Boot.getLogger().log("Finished batchConvertProcess with session name: " + Boot.getSessionOfThisRun().getName(), true);
	}

	private ProfileElement scrapeToProfileElement(Scrape scrape) throws Exception {
		//Boot.getLogger().log("Covert scrape id " + scrape.id +  ", URL " + scrape.url, false);
		
		// Check for scrape URL validity.
		Matcher m = USER_URL_PATTERN.matcher(scrape.url);
		if (m.matches()) {
			Document profilePageDoc = Jsoup.parse(scrape.content);
			int ffId = Integer.parseInt(m.group("ffId"));
			ProfileElement profileElement = new ProfileElement(profilePageDoc, ffId);
			return profileElement;
		}
		
		throw new Exception("Scrape id " + scrape.id + ", URL " + scrape.url + " did not match USER_URL_PATTERN");
	}

}
