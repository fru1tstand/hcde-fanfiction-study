package me.fru1t.fanfiction.process;

import java.util.Date;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.schema.Scrape;
import me.fru1t.fanfiction.database.schema.scrape.BufferedRawScrapeProducer;
import me.fru1t.fanfiction.web.page.BookSearchPage;

public class ExtractBooksListDataProcess implements Runnable {
	private static final String PROCESS_SESSION_NAME =
			"Harry Potter, English, April 7, 2016 - final";
	private static final String[] SCRAPE_SESSION_NAME =
			{"Harry Potter, English, March 29, 2016"};
	
	@Override
	public void run() {
		BookSearchPage bsp;
		
		BufferedRawScrapeProducer brsp = new BufferedRawScrapeProducer(-1, 11, SCRAPE_SESSION_NAME);
		Scrape.ScrapeRaw scrape = brsp.take();
		long beforeTime = (new Date()).getTime();
		while (scrape != null) {
			bsp = new BookSearchPage(scrape.content);
			Scrape.uspScrapeAddProcessedBookResultElement(
					scrape.id, PROCESS_SESSION_NAME, bsp.getBookResultElements());
			scrape = brsp.take();
		}
		long afterTime = (new Date()).getTime();
		Boot.log("That session took " + (afterTime - beforeTime) + "ms");
	}

}
