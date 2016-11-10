package me.fru1t.fanfiction.process.scrape;

import org.eclipse.jdt.annotation.Nullable;
import java.util.ArrayList;
import me.fru1t.util.concurrent.ConcurrentProducer;

public class TxtFileBasedUrlProducer extends ConcurrentProducer<String> {
	/**
	 * Read a text file that contains a list of urls,
	 * and output it line by line to the ScrapeProcesser
	 * 
	 */
	private static boolean isComplete;
	private int urlIndex;
	private ArrayList<String> urlList;
	
	public TxtFileBasedUrlProducer(ArrayList<String> urlList) {
		isComplete = false;
		urlIndex = 0;
		this.urlList = urlList;
	}

	@Override
	@Nullable
	public synchronized String take() {
		if (isComplete) {
			return null;
		}
		
		// Check if we still have pages ready to serve
		if (urlIndex < urlList.size()) {
			return urlList.get(urlIndex++);
		}
		
		isComplete = true;
		return null;
	}


	@Override
	public boolean isComplete() {
		return isComplete;
	}
}