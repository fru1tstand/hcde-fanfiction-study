package me.fru1t.fanfiction.process;

//import org.eclipse.jdt.annotation.Nullable;
//
//import me.fru1t.fanfiction.Boot;
//import me.fru1t.fanfiction.database.producers.RawScrapeProducer;
//import me.fru1t.fanfiction.web.page.BookSearchPage;

/**
 * Fix before use
 * @deprecated
 */
public class ExtractBooksListDataProcess implements Runnable {
//	private static final String PROCESS_SESSION_NAME =
//			"Harry Potter, English, April 8, 2016 - final";
//	@Nullable
//	private static final String[] SCRAPE_SESSION_NAME =
//			{"Harry Potter, English, March 29, 2016"};
	
	@Override
	public void run() {
//		BookSearchPage bsp;
//		RawScrapeProducer brsp = new RawScrapeProducer(SCRAPE_SESSION_NAME);
//		Scrape.ScrapeRaw scrape = brsp.take();
//		while (scrape != null) {
			try {
//				bsp = new BookSearchPage(scrape.content);
//				Scrape.uspScrapeAddProcessedBookResultElement(
//						scrape.id, PROCESS_SESSION_NAME, bsp.getBookResultElements());
			} catch (Exception e) {
//				Boot.getLogger().log(e, "Skipped scrape with ID " + scrape.id + " due to:");
			}
//			scrape = brsp.take();
//		}
	}
}
