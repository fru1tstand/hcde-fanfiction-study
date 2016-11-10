package me.fru1t.fanfiction.process.convert;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.database.UserToProfileProcedures;
import me.fru1t.fanfiction.database.producers.ScrapeProducer.Scrape;
import me.fru1t.fanfiction.web.page.element.ProfileElement;
import me.fru1t.util.Consumer;

/**
 * This class defines a consumer which eats profile pages,
 * extract user information (e.g. age) and store it within the database.
 */
public class UserToProfiles extends Consumer<Scrape> {
	
	// Matches profile URLs with or without filters.
	private static final Pattern USER_URL_PATTERN =
			Pattern.compile("^https://www.fanfiction.net/u/([0-9]+)$");

	private static Pattern USER_URL_PATTERN_GROUP = 
			Pattern.compile("^//www.fanfiction.net/u/(?<ffId>[0-9]+)/(?<userName>.*)$");
	
	@Override
	public void eat(Scrape scrape) {
		long startTime = (new Date()).getTime();
		
		// Check for scrape URL validity.
		Matcher m = USER_URL_PATTERN.matcher(scrape.url);
		if (!m.matches()) {
			Boot.getLogger().log("Invalid URL for Profile, ignoring: " + scrape.url, true);
			return;
		}
		
		try {
			Document profilePageDoc = Jsoup.parse(scrape.content);

			// parse for ff_id and user_name that cannot be null
			m = USER_URL_PATTERN_GROUP.matcher(profilePageDoc.select("link[rel=canonical]").attr("href"));
			if (m.matches()) {			
				int ffId = Integer.parseInt(m.group("ffId"));
				String userName = m.group("userName");
				ProfileElement profileElement = new ProfileElement(profilePageDoc, ffId, userName);
				UserToProfileProcedures.processUserScrapeToProfile(profileElement);
				
				Boot.getLogger().log("Processed url for user ff_id /" + ffId
						+ "; Took: " + ((new Date()).getTime() - startTime) + "ms", true);
			} else {
				throw new Exception("Could not extract ffId and userName from the canonical link. url was: " + scrape.url);
			}
			
		} catch (NumberFormatException e) {
			Boot.getLogger().log(e, "Invalid ffId in url " + scrape.url + " with details: ");
		} catch (Exception e) {
			Boot.getLogger().log(e, "Skipped scrape with ID " + scrape.id + " due to:");
		}
		
		
	}
	
	

}
