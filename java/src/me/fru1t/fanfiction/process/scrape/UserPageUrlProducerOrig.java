package me.fru1t.fanfiction.process.scrape;

import org.eclipse.jdt.annotation.Nullable;

import me.fru1t.fanfiction.database.producers.ProfileProducer.Profile;
import me.fru1t.util.Producer;
import me.fru1t.util.concurrent.ConcurrentProducer;

public class UserPageUrlProducerOrig extends ConcurrentProducer<String> {

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
	private Producer<Profile> profileProducer;
	private Profile currentProfile;
	
	public UserPageUrlProducerOrig(Producer<Profile> profileProducer) {
		this.profileProducer = profileProducer;
		this.currentProfile = null;
		isComplete = false;
	}


	@Override
	public @Nullable String take() {
		if (isComplete) {
			return null;
		}
		
		// Check if we still have profiles to scrape
		currentProfile = profileProducer.take();
		
		if (currentProfile != null) {
			return getProfileUrl(currentProfile);
		}
		
		// else we are finished with scraping profiles
		isComplete = true;
		return null;
	}
	
	
	@Override
	public boolean isComplete() {
		return isComplete;
	}
	
	
	private String getProfileUrl(Profile profile) {
		return String.format(USER_BASE_URL, profile.ff_id);
	}
}