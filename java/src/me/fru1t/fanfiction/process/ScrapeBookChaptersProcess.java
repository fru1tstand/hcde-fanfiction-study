package me.fru1t.fanfiction.process;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.producers.BookIdAndChaptersProducer;
import me.fru1t.fanfiction.database.producers.BookIdAndChaptersProducer.BookIdAndChapters;

/**
 * Fix before use
 * @deprecated
 */
public class ScrapeBookChaptersProcess implements Runnable {
	private static final String BOOK_CHAPTER_URL_FMT = "https://www.fanfiction.net/s/%d/%d";
	private static final String SESSION_NAME =
			"Harry Potter, English, Account > 5 years old, > 10 reviews, > 2 reviews/chapter, April 26, 2016 - Fix 2";
	private static final int LAST_ID_FAULT = 120919;
//	private static final String SCRAPE_TYPE = "book-chapter";
	private static final double AVG_SLEEP_TIME_PER_IP = 7.5;
	private static final double STDEV = 1.0;
	private static final byte[][] ips = {
			{ (byte) 104, (byte) 128, (byte) 237, (byte) 128 },
			{ (byte) 104, (byte) 128, (byte) 233, (byte) 73 },
			{ (byte) 45, (byte) 58, (byte) 54, (byte) 250 }
	};
//	private static final byte[][] ips = {
//			{ (byte) 192, (byte) 168, 1, 107 }
//	};
	
	private Set<Integer> processedBookIds;
	private Stack<String> urlsToProcess;
	
	@Override
	public void run() {
		Boot.getLogger().log("Running ScrapeBookChaptersProcess with Session Name " + SESSION_NAME);
//		MultiIPCrawler crawler = new MultiIPCrawler(ips);
		Random rand = new Random();
		processedBookIds = new HashSet<>();
		urlsToProcess = new Stack<>();
		BookIdAndChaptersProducer producer = new BookIdAndChaptersProducer();
		producer.startAt(LAST_ID_FAULT);
		
		BookIdAndChapters book = null;
//		String urlToProcess = null;
		int i = 0;
		int waitTime = 0;
		while (true) {
			book = producer.take();
			if (book == null) {
				Boot.getLogger().log("T-t-t-that's all folks -- No more books in queue");
				break;
			}
			if (processedBookIds.contains(book.bookId)) {
				Boot.getLogger().log("Ignoring repeat book " + book.bookId);
				continue;
			}
			processedBookIds.add(book.bookId);
			StringBuilder status = new StringBuilder();
			status.append("id: " + book.id 
					+ "; bookid: " + book.bookId 
					+ "; Chapters: " + book.metaChapters 
					+ "; Wait times: ");
			
			for (i = 1; i <= book.metaChapters; i++) {
				urlsToProcess.add(String.format(BOOK_CHAPTER_URL_FMT, book.bookId, i));
			}
			while (!urlsToProcess.isEmpty()) {
//				urlToProcess = urlsToProcess.pop();
//				Scrape.insertRaw(SESSION_NAME, SCRAPE_TYPE,
//						urlToProcess, crawler.getContents(urlToProcess));
				try {
					waitTime = Math.max(0, 
							(int) ((rand.nextGaussian() * STDEV 
									+ (AVG_SLEEP_TIME_PER_IP / ips.length)) 
									* 1000));
					status.append(waitTime + ", ");
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					Boot.getLogger().log(e, "Last scrape status: " + status.toString());
					return;
				}
			}
			Boot.getLogger().log(status.toString());
		}
	}
}
