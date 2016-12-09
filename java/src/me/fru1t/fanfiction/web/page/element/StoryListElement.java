package me.fru1t.fanfiction.web.page.element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import me.fru1t.fanfiction.Boot;
import me.fru1t.util.FFElement;
import me.fru1t.util.Preconditions;

public class StoryListElement extends FFElement {
	public static class Metadata {
		private static final boolean LOG_COMPONENT_IGNORES = true;
		
		// Text processing regex
		private static final Pattern NUMBER_SANITIZATION_PATTERN =
				Pattern.compile("[^0-9]");
		private static final Pattern COMPONENT_KEY_VALUE_PATTERN =
				Pattern.compile("(\\w+): ([a-zA-Z0-9/,+]+)");
		private static final Pattern CHARACTER_MATCH_PATTERN =
				Pattern.compile("(\\w[^,\\[\\]]+)");
		
		// Test processing values
		private static final String COMPONENTS_DELIMETER = " - ";
		private static final String GENRES_DELIMETER = "/";
		private static final String COMPLETE_COMPONENT_VALUE = "Complete";
		
		// Indexes (these never change and are always present)
		private static final int RATING_INDEX = 0;
		private static final int LANGUAGE_INDEX = 1;
		private static final int GENRE_INDEX = 2;
		private static final int CHAPTERS_INDEX_WITHOUT_GENRES = 2;
		private static final int CHAPTERS_INDEX_WITH_GENRES = 3;
		
		// Chapters - words - reviews - favs - follows
		private static final int STATIC_METADATA_COUNT = 5;

		// Prefixes (these are optionally included)
		private static final String RATING_PREFIX = "Rated";
		private static final String CHAPTERS_PREFIX = "Chapters";
		private static final String WORDS_PREFIX = "Words";
		private static final String REVIEWS_PREFIX = "Reviews";
		private static final String FAVORITES_PREFIX = "Favs";
		private static final String FOLLOWERS_PREFIX = "Follows";
		private static final String UPDATED_PREFIX = "Updated";
		private static final String PUBLISHED_PREFIX = "Published";
		
		// Date processing
		private static final String DATES_SELECTOR = "span";

		// Components
		public Element element;
		public String rating;
		public String language;
		public String[] genres;
		public int chapters;
		public int words;
		public int reviews;
		public int favorites;
		public int followers;
		public int dateUpdated;
		public int datePublished;
		public List<String> characters;
		public boolean isComplete;
		
		
		public Metadata(Element metadata) throws Exception {
			this.element = metadata;
			this.rating = "";
			this.language = "";
			this.genres = new String[0];
			this.chapters = 0;
			this.words = 0;
			this.reviews = 0;
			this.favorites = 0;
			this.followers = 0;
			this.datePublished = -1;
			this.dateUpdated = -1;
			this.characters = new ArrayList<>();
			this.isComplete = false;
			
			String[] components = metadata.text().split(COMPONENTS_DELIMETER);
			
			// Rating and language always exist
			processComponent(components[RATING_INDEX]);
			this.language = components[LANGUAGE_INDEX];
			
			// Check if genres exist
			int chaptersIndex = CHAPTERS_INDEX_WITHOUT_GENRES;
			if (!components[GENRE_INDEX].contains(CHAPTERS_PREFIX)) {
				this.genres = components[GENRE_INDEX].split(GENRES_DELIMETER);
				chaptersIndex = CHAPTERS_INDEX_WITH_GENRES;
			}
			
			// Process the [up to] 5 static metadata objects
			for (int i = chaptersIndex; i < chaptersIndex + STATIC_METADATA_COUNT; i++) {
				if (!processComponent(components[i])) {
					break;
				}
			}
			
			// Date parsing
			// Published date will always appear, but update date is optional.
			Elements dateElements = metadata.select(DATES_SELECTOR);
			if (!Preconditions.isWithin(dateElements.size(), 1, 2)) {
				throw new Exception(dateElements.size()
						+ " date elements were found when only expecting 1 or 2.");
			}
			this.datePublished = sanitizeInteger(dateElements.last().attr("data-xutime"));
			if (dateElements.size() == 2) {
				this.dateUpdated = sanitizeInteger(dateElements.get(0).attr("data-xutime"));
			}
			
			// Characters / Complete
			Node afterDateText = dateElements.last().nextSibling();
			if (afterDateText != null) {
				// This should ever only contain 2 or 3 elements where the first element is always
				// empty. That last one or two can be the success or character components.
				String[] afterDateComponents = afterDateText.toString().split(COMPONENTS_DELIMETER);
				
				if (afterDateComponents.length == 2) {
					// It's one or the other.
					if (afterDateComponents[1].equals(COMPLETE_COMPONENT_VALUE)) {
						this.isComplete = true;
					} else {
						processCharacters(afterDateComponents[1]);
					}
				} else if (afterDateComponents.length == 3) {
					// Both, but characters is always 2nd element
					this.isComplete = true;
					processCharacters(afterDateComponents[1]);
				} else if (afterDateComponents.length > 3) {
					this.isComplete = true;
					processCharacters(afterDateComponents[1]);
					System.out.println("[Warning] Found " + afterDateComponents.length 
							+ " elements with text: " + afterDateText.toString() + ". "
							+ "Processing `" + afterDateComponents[1] + "` as the character component." );
				}/* else {
					throw new Exception(afterDateComponents.length
							+ " after date components were found when only expecting 2 or 3 or 4.");
				} */
			}
		}

		private void processCharacters(String charactersComponent) {
			Matcher m = CHARACTER_MATCH_PATTERN.matcher(charactersComponent);
			while (m.find()) {
				characters.add(m.group(1));
			}
		}
		
		private boolean processComponent(String component) {
			Matcher m = COMPONENT_KEY_VALUE_PATTERN.matcher(component);
			if (!m.matches()) {
				if (LOG_COMPONENT_IGNORES) {
					Boot.getLogger().log("Ignoring component: " + component, true);
				}
				return false;
			}
			switch (m.group(1)) {
			case RATING_PREFIX:
				this.rating = m.group(2);
				return true;
			case CHAPTERS_PREFIX:
				this.chapters = sanitizeInteger(m.group(2));
				return true;
			case WORDS_PREFIX:
				this.words = sanitizeInteger(m.group(2));
				return true;
			case REVIEWS_PREFIX:
				this.reviews = sanitizeInteger(m.group(2));
				return true;
			case FAVORITES_PREFIX:
				this.favorites = sanitizeInteger(m.group(2));
				return true;
			case FOLLOWERS_PREFIX:
				this.followers = sanitizeInteger(m.group(2));
				return true;
			case UPDATED_PREFIX:
			case PUBLISHED_PREFIX:
				return false;
			default:
				if (LOG_COMPONENT_IGNORES) {
					Boot.getLogger().log("Ignoring component: " + component, true);
				}
				break;
			}
			return false;
		}

		/**
		 * Converts a string integer into an int type, stripping all non-numeric characters;
		 * specifically, commas.
		 */
		private static int sanitizeInteger(String s) {
			Matcher intMatcher = NUMBER_SANITIZATION_PATTERN.matcher(s);
			return Integer.parseInt(intMatcher.replaceAll(""));
		}
	}
	
	// HTML Selectors
	private static final String TITLE_SELECTOR = "a.stitle";
	private static final String COVER_IMAGE_SELECTOR = "a.stitle img";
	private static final String AUTHOR_SELECTOR = "a[href^=\"/u/\"]";
	private static final String SYNOPSIS_SELECTOR = ".z-indent.z-padtop";
	private static final String METADATA_SELECTOR = ".z-indent.z-padtop .z-padtop2.xgray";
	
	// Processing regex
	private static final Pattern AUTHOR_ID_LINK_PATTERN = Pattern.compile("/u/(\\d+).+");
	private static final Pattern BOOK_ID_LINK_PATTERN = Pattern.compile("/s/(\\d+).+");
	
	
	// Direct element values
	public Element element;
	public String bookTitle;
	public String bookUrl;
	public String coverImageUrl;
	public String coverImageOriginalUrl;
	public String user_name;
	public String authorUrl;
	public String synopsis;
	public String metadata;
	
	// Processed values
	public int ffUserId;
	public int ffBookId;
	public Metadata processedMetadata;
	
	@Override
	public String toString() {
		return "Meta: " + metadata;
	}

	
	public StoryListElement(Element result) throws Exception {
		this.element = result;
		
		this.ffBookId = -1;
		this.bookTitle = null;
		this.bookUrl = null;
		
		this.ffUserId = -1;
		this.user_name = null;
		this.authorUrl = null;
		
		this.synopsis = null;
		this.metadata = null;

		this.coverImageUrl = null;
		this.coverImageOriginalUrl = null;
		

		// Direct Element values
		if (result.select(TITLE_SELECTOR).size() > 0) {
			Element title = result.select(TITLE_SELECTOR).get(0);
			this.bookTitle = title.ownText();
			this.bookUrl = title.attr("href");
			Matcher bookIdMatcher = BOOK_ID_LINK_PATTERN.matcher(bookUrl);
			bookIdMatcher.matches();
			this.ffBookId = Integer.parseInt(bookIdMatcher.group(1));
		}
		
		if (result.select(COVER_IMAGE_SELECTOR).size() > 0) {
			Element coverImage = result.select(COVER_IMAGE_SELECTOR).get(0);
			this.coverImageUrl = coverImage.attr("src");
			this.coverImageOriginalUrl = coverImage.attr("data-original");
		}

		// Some stories have no author link indicated, and 
		// in such case, story link is not locatable as well 
		// e.g. https://www.fanfiction.net/misc/Misc-Plays-Musicals/?&srt=2&r=10&p=648's 
		// https://www.fanfiction.net/s/407743/1/The-Hukilau
		if (result.select(AUTHOR_SELECTOR).size() > 0) {
			Element author = result.select(AUTHOR_SELECTOR).get(0);
			this.user_name = author.ownText();
			this.authorUrl = author.attr("href");
			Matcher authorIdMatcher = AUTHOR_ID_LINK_PATTERN.matcher(authorUrl);
			authorIdMatcher.matches();
			this.ffUserId = Integer.parseInt(authorIdMatcher.group(1));
		}

		if (result.select(SYNOPSIS_SELECTOR).size() > 0) {
			this.synopsis = result.select(SYNOPSIS_SELECTOR).get(0).ownText();
		}

		// Metadata
		if (result.select(METADATA_SELECTOR).size() > 0) {
			Element metadata = result.select(METADATA_SELECTOR).get(0);
			this.metadata = metadata.text();
			this.processedMetadata = new Metadata(metadata);
		}

	}
}
