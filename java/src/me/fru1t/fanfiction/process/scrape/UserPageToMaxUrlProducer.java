package me.fru1t.fanfiction.process.scrape;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.util.concurrent.ConcurrentProducer;

public class UserPageToMaxUrlProducer extends ConcurrentProducer<String> {

	/**
	 * example url: 
	 * https://www.fanfiction.net/u/3922474
	 * 
	 * Note: https://www.fanfiction.net/u/3922474/darkWarrior101 is also a valid, 
	 * but since users can change their username, only going to use the id number
	 * 
	 */
	private static final String USER_BASE_URL = "https://www.fanfiction.net/u/%d";
	private static boolean isComplete;
	// private static final int MAX_FFID = 8520203;
	
	private int currentFFId;
	private int upperBoundFFId;
	
	public UserPageToMaxUrlProducer(int MAX_FFID) {
		this(1, MAX_FFID, MAX_FFID);
	}
	
	public UserPageToMaxUrlProducer(int startId, int endId, int MAX_FFID) {
		this.currentFFId = startId;
		this.upperBoundFFId = Integer.min(MAX_FFID, endId);
		
		isComplete = false;
	}


	@Override
	public @Nullable String take() {
		if (isComplete) {
			return null;
		}
		
		if (currentFFId <= upperBoundFFId) {
			return getProfileUrl(currentFFId++);
		}
		
		// else we are finished with scraping profiles
		isComplete = true;
		return null;
	}
	
	
	@Override
	public boolean isComplete() {
		return isComplete;
	}
	
	
	private String getProfileUrl(int ffId) {
		return String.format(USER_BASE_URL, ffId);
	}
}