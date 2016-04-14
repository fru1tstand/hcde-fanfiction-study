package me.fru1t.fanfiction.web.page.element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import me.fru1t.fanfiction.Boot;

public class BookResultElement {
	public static class Metadata {
		private static final boolean LOG_COMPONENT_IGNORES = true;
		
		// Text processing regex
		private static final Pattern NUMBER_SANITIZATION_PATTERN =
				Pattern.compile("[^0-9]");
		private static final Pattern COMPONENT_KEY_VALUE_PATTERN =
				Pattern.compile("(\\w+): ([a-zA-Z0-9,+]+)");
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
		private static final int FIRST_NON_STATIC_INDEX = 3;
		
		// Prefixes (these are optionally included)
		private static final String RATING_PREFIX = "Rated";
		private static final String CHAPTERS_PREFIX = "Chapters";
		private static final String WORDS_PREFIX = "Words";
		private static final String REVIEWS_PREFIX = "Reviews";
		private static final String FAVORITES_PREFIX = "Favs";
		private static final String FOLLOWERS_PREFIX = "Follows";
		
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
		
		public boolean didSuccessfullyParse;
		
		public Metadata(Element metadata) throws Exception {
			this.didSuccessfullyParse = false;
			this.element = metadata;
			this.rating = "";
			this.language = "";
			this.genres = new String[0];
			this.chapters = -1;
			this.words = -1;
			this.reviews = -1;
			this.favorites = -1;
			this.followers = -1;
			this.datePublished = -1;
			this.dateUpdated = -1;
			this.characters = new ArrayList<>();
			this.isComplete = false;
			
			int firstNonStaticIndex = FIRST_NON_STATIC_INDEX;
			String[] components = metadata.text().split(COMPONENTS_DELIMETER);
			processComponent(components[RATING_INDEX]);
			this.language = components[LANGUAGE_INDEX];
			if (components[GENRE_INDEX].contains(CHAPTERS_PREFIX)) {
				this.genres = components[GENRE_INDEX].split(GENRES_DELIMETER);
				firstNonStaticIndex--;
			}
			
			for (int i = firstNonStaticIndex; i < components.length; i++) {
				processComponent(components[i]);
			}
			
			// Date parsing
			// Published date will always appear, but update date is optional. Components that
			// appear after dates are all optional and include: characters and completion.
			Elements dateElements = metadata.select(DATES_SELECTOR);
			if (dateElements.size() < 1 || dateElements.size() > 2) {
				throw new Exception(dateElements.size()
						+ " date elements were found when only expecting 1 or 2.");
			}
			this.datePublished = sanitizeInteger(dateElements.last().attr("data-xutime"));
			if (dateElements.size() == 2) {
				this.dateUpdated = sanitizeInteger(dateElements.get(0).attr("data-xutime"));
			}
			
			// After date parsing: characters and completion
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
				} else {
					throw new Exception(afterDateComponents.length
							+ " after date components were found when only expecting 2 or 3.");
				}
			}
			this.didSuccessfullyParse = true;
		}
		
		private void processCharacters(String charactersComponent) {
			Matcher m = CHARACTER_MATCH_PATTERN.matcher(charactersComponent);
			while (m.find()) {
				characters.add(m.group(1));
			}
		}
		
		private void processComponent(String component) {
			Matcher m = COMPONENT_KEY_VALUE_PATTERN.matcher(component);
			if (!m.matches()) {
				if (LOG_COMPONENT_IGNORES) {
					Boot.log("Ignoring component: " + component);
				}
				return;
			}
			switch (m.group(1)) {
			case RATING_PREFIX:
				this.rating = m.group(2);
				break;
			case CHAPTERS_PREFIX:
				this.chapters = sanitizeInteger(m.group(2));
				break;
			case WORDS_PREFIX:
				this.words = sanitizeInteger(m.group(2));
				break;
			case REVIEWS_PREFIX:
				this.reviews = sanitizeInteger(m.group(2));
				break;
			case FAVORITES_PREFIX:
				this.favorites = sanitizeInteger(m.group(2));
				break;
			case FOLLOWERS_PREFIX:
				this.followers = sanitizeInteger(m.group(2));
				break;
			default:
				if (LOG_COMPONENT_IGNORES) {
					Boot.log("Ignoring component: " + component);
				}
				break;
			}
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
	public String realBookName;
	public String bookTitle;
	public String bookUrl;
	public String coverImageUrl;
	public String coverImageOriginalUrl;
	public String author;
	public String authorUrl;
	public String synopsis;
	public String metadata;
	
	// Processed values
	public int ffAuthorId;
	public int ffBookId;
	public Metadata processedMetadata;
	
	public boolean didSuccessfullyParse;
	
	public BookResultElement(String realBookName, Element result) {
		this.realBookName = realBookName;
		this.didSuccessfullyParse = false;
		this.element = result;
		this.bookTitle = "";
		this.bookUrl = "";
		this.coverImageUrl = "";
		this.coverImageOriginalUrl = "";
		this.author = "";
		this.authorUrl = "";
		this.synopsis = "";
		this.metadata = "";
		this.ffAuthorId = -1;
		this.ffBookId = -1;
		
		try {
			// Direct Element values
			Element title = result.select(TITLE_SELECTOR).get(0);
			this.bookTitle = title.ownText();
			this.bookUrl = title.attr("href");
			
			Element coverImage = result.select(COVER_IMAGE_SELECTOR).get(0);
			this.coverImageUrl = coverImage.attr("src");
			this.coverImageOriginalUrl = coverImage.attr("data-original");
			
			Element author = result.select(AUTHOR_SELECTOR).get(0);
			this.author = author.ownText();
			this.authorUrl = author.attr("href");
			
			this.synopsis = result.select(SYNOPSIS_SELECTOR).get(0).ownText();
			
			// Processed values
			Matcher authorIdMatcher = AUTHOR_ID_LINK_PATTERN.matcher(authorUrl);
			Matcher bookIdMatcher = BOOK_ID_LINK_PATTERN.matcher(bookUrl);
			authorIdMatcher.matches();
			bookIdMatcher.matches();
			this.ffAuthorId = Integer.parseInt(authorIdMatcher.group(1));
			this.ffBookId = Integer.parseInt(bookIdMatcher.group(1));
			
			// Metadata
			Element metadata = result.select(METADATA_SELECTOR).get(0);
			this.metadata = metadata.text();
			this.processedMetadata = new Metadata(metadata);
			
			this.didSuccessfullyParse = true;
		} catch (IndexOutOfBoundsException e) {
			Boot.log(e, "A required element wasn't found on the page.");
		} catch (Exception e) {
			Boot.log(e, "An unknown exception occured");
		}
	}
}
