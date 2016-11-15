package me.fru1t.fanfiction.web.page.element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ProfileElement {
	private static String age_arabic = "([0-9]{1,2})";
	private static String age_words = "(ten|eleven|twelve|thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|nineteen|"
									+ "twenty one|twenty two|twenty three|twenty four|twenty five|twenty six|twenty seven|twenty eight|twenty nine|twenty|"
									+ "thirty one|thirty two|thirty three|thirty four|thirty five|thirty six|thirty seven|thirty eight|thirty nine|thirty|"
									+ "forty one|forty two|forty three|forty four|forty five|forty six|forty seven|forty eight|forty nine|forty|"
									+ "fifty one|fifty two|fifty three|fifty four|fifty five|fifty six|fifty seven|fifty eight|fifty nine|fifty|"
									+ "sixty one|sixty two|sixty three|sixty four|sixty five|sixty six|sixty seven|sixty eight|sixty nine|sixty|"
									+ "seventy one|seventy two|seventy three|seventy four|seventy five|seventy six|seventy seven|seventy eight|seventy nine|seventy|"
									+ "eighty one|eighty two|eighty three|eighty four|eighty five|eighty six|eighty seven|eighty eight|eighty nine|eighty|"
									+ "ninety one|ninety two|ninety three|ninety four|ninety five|ninety six|ninety seven|ninety eight|ninety nine|ninety|"
									+ "one|two|three|four|five|six|seven|eight|nine)";
	private static String AGE_PATTERN_GROUP = "(?<userAge>" + age_arabic + "|" + age_words + ")";
	
	private static String gender_words = "(female|male|boy|girl|woman|man)";
	private static String GENDER_PATTERN_GROUP = "(?<userGender>" + gender_words + ")";
	
	private int scrapeid; // used for debugging
	
	public int my_ff_id; // NOT NULL
	public String user_name;
	public String country_name;
	public int join_date;
	public int update_date;
	public String bio;
	public int age;
	public String gender; // going to be either "female" or "male";

	public int getScrapeId() {
		return this.scrapeid;
	}
	public void setScrapeid(int sid) {
		this.scrapeid = sid;
	}
	
	public static class FavAuthor {
		public int ff_id;
		public String name;
		public FavAuthor(int ff_id, String name) {
			this.ff_id = ff_id;
			this.name = name;
		}
		
		public boolean equals(FavAuthor a) {
			return Integer.compare(this.ff_id, a.ff_id) == 0 ? true : false;
		}
		
		public int hashCode() { 
			return ("" + ff_id).hashCode(); 
		}

		public String toString() {
			return "ff_id: " + ff_id + ", name: " + name;
		}
	}
	
	public Set<FavAuthor> myFavAuthors;
	public Set<Integer> myFavStories;
	
	public ProfileElement(Document profilePageDoc, int my_ff_id) {
		this.my_ff_id = my_ff_id;
		this.country_name = null;
		this.join_date = -1;
		this.update_date = -1;
		this.bio = null;
		this.age = -1;
		this.gender = null;
		this.myFavAuthors = new HashSet<>();
		this.myFavStories = new HashSet<>();
		
		if (this.checkErrorPage(profilePageDoc)) return;
		
		// may not be public, in this case empty string returned
		//this.country_name = profilePageDoc.select("div#content_wrapper_inner table table td").eq(1).select("img").attr("title");
		this.country_name = profilePageDoc.select("div#content_wrapper_inner table table td[colspan=2]")
										  .select("img").attr("title");
		this.getJoinDateAndUpdateDate(profilePageDoc);
		this.bio = profilePageDoc.select("div#bio").html();
		this.age = extractAge(profilePageDoc);
		this.gender = extractGender(profilePageDoc);
		this.getFavAuthors(profilePageDoc);
		this.getFavStories(profilePageDoc);
	}
	
	public ProfileElement(Document profilePageDoc, int my_ff_id, String user_name) {
		this(profilePageDoc, my_ff_id);
		this.user_name = user_name;
	}
	
	private boolean checkErrorPage(Document profilePageDoc) {
		String msg = profilePageDoc.select("div.panel_normal span.gui_normal").text();		
		Pattern ERROR_PATTERN = Pattern.compile("^FanFiction.Net Message Type .*");
		return ERROR_PATTERN.matcher(msg).matches();
	}
	
	/**
	 * Joined date will always appear, but update date is optional.
	 * @param profilePageDoc
	 * @throws Exception
	 */
	private void getJoinDateAndUpdateDate(Document profilePageDoc) {
		Elements dateElements = 
				profilePageDoc.select("div#content_wrapper_inner table table td[colspan=2] span[data-xutime]");
		int dateElementSize = dateElements.size();
		if (dateElementSize == 2) {
			this.join_date = sanitizeInteger(dateElements.first().attr("data-xutime"));
			this.update_date = sanitizeInteger(dateElements.get(1).attr("data-xutime"));
		} else if (dateElementSize == 1) {
			this.join_date = sanitizeInteger(dateElements.first().attr("data-xutime"));
		}
	}
	
	private int extractAge(Document profilePageDoc) {
		String[] patterns = {
			"(.* |^)((i) (am)|(i'm))" + "(.*and)? " + AGE_PATTERN_GROUP + ".*",
			"(.* |^)((my )?(age)( )?(:|is)?( )?)" + AGE_PATTERN_GROUP + ".*",
			"(.* |^)" + AGE_PATTERN_GROUP + " (yrs|years|year) (old).*"
			//".*" + i_am + " in (my) (late|mid|early) " + age_words_tens + ".*"
		};
		
		String patternAvg = "(.* |^)((my )?(age)( )?(:|is)?( )?)" 
						+ "(?<userAgeOne>" + age_arabic + "|" + age_words + ")" + "-" 
						+ "(?<userAgeTwo>" + age_arabic + "|" + age_words + ")" +".*";

		Elements bioParagraphs = profilePageDoc.select("div#bio p");
		for (Element bioPara : bioParagraphs) {
			String line = bioPara.text();

			// Handle the average case e.g. Age: 18-25
			// link - https://www.fanfiction.net/u/2623032/FayTheGay 
			Pattern p = Pattern.compile(patternAvg, Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(line);
			if (m.matches()) {
				return (word2number(m.group("userAgeTwo")) + word2number(m.group("userAgeOne")))/2;
			}
			
			for (String pattern : patterns) {
				p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
				m = p.matcher(line);
				if (m.matches()) {
					return word2number(m.group("userAge"));
				}
			}
			
		}

		return -1;
	}
	
	private String extractGender(Document profilePageDoc) {
		String[] patterns = {
			"(.* |^)((i) (am)|(i'm))" + "( a)? " + GENDER_PATTERN_GROUP + ".*",
			"(.* |^)((my )?(gender)( )?(:|is)?( )?)" + GENDER_PATTERN_GROUP + ".*",
			"(.* |^)((my )?(sex)( )?(:|is)?( )?)" + GENDER_PATTERN_GROUP + ".*",
			"(.* |^)((my )?(sexuality)( )?(:|is)?( )?)" + GENDER_PATTERN_GROUP + ".*"
		};
		
		Elements bioParagraphs = profilePageDoc.select("div#bio p");
		for (Element bioPara : bioParagraphs) {
			String line = bioPara.text();
			for (String pattern : patterns) {
				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(line);
				if (m.matches()) {
					return change2fm(m.group("userGender"));
				}
			}
		}
		return null;
	}

	private void getFavAuthors(Document profilePageDoc) {
		for (Element elem : profilePageDoc.select("div#fa table dl")) {
			String u_html = elem.select("a").attr("href"); // e.g. '/u/2289300/Paimpont'
			Matcher m = Pattern.compile("^/u/(?<favUserId>[0-9]+)/(.*)$").matcher(u_html);
			if (m.find()) {
				int fav_user_id = Integer.parseInt(m.group("favUserId"));
				String fav_user_name = elem.select("a").text();
				this.myFavAuthors.add(new FavAuthor(fav_user_id, fav_user_name));
			}
		}
	}
	
	private void getFavStories(Document profilePageDoc) {
		for (Element elem : profilePageDoc.select("div#fs_inside div.favstories")) {
			int fav_story_id = Integer.parseInt(elem.attr("data-storyid"));
			this.myFavStories.add(fav_story_id);
		}
	}
	
	/**
	 * Converts a string integer into an int type, stripping all non-numeric characters;
	 * specifically, commas.
	 */
	private static int sanitizeInteger(String s) {
		Matcher intMatcher = Pattern.compile("[^0-9]").matcher(s);
		return Integer.parseInt(intMatcher.replaceAll(""));
	}

	private static int word2number(String word) {
		if (word == null) return -1;
		
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		String[] tens = {"", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};
		String[] ones = {"", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
		String[] rest = {"ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
		for (int i = 0; i < tens.length; i++) {
			for (int j = 0; j < ones.length; j++) {
				String one = ones[j], ten = tens[i];
				
				if (one.equals("") && ten.equals("")) continue;
				else if (ten.equals("")) map.put(one, i);
				else if (one.equals("")) map.put(ten, (j+1)*10);
				else map.put(ten + " " + one, (j+1)*10 + i); 
			}
		}
		for (int i = 0; i < rest.length; i++) {
			map.put(rest[i], i + 10);
		}
		
		for (int i = 1; i < 100; i++) {
			map.put("" + i, i);
		}
		
 		if (map.containsKey(word)) return map.get(word);
		return 0;
	}

	private static String change2fm(String input) {
		input = input.toLowerCase();
		if (input.equals("female") || input.equals("woman") || input.equals("girl"))
			return "female";
		else if (input.equals("male") || input.equals("man") || input.equals("boy"))
			return "male";
	
		return null;
	}
	
}