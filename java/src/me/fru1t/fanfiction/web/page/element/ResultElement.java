package me.fru1t.fanfiction.web.page.element;

import org.jsoup.nodes.Element;

public class ResultElement {
	private static final String TITLE_SELECTOR = "a.stitle";
	private static final String COVER_IMAGE_SELECTOR = "a.stitle img.lazy.cimg";
	
	public class Processed {
		public String authorId;
		public String bookId;
		
		// Metadata
		public String rating;
		public String language;
		public String[] genres;
		public int chapters;
		public int words;
		public int reviews;
		public int favorites;
		public int follows;
		public int dateUpdated;
		public int datePublished;
		public String[] characters;
		public boolean isComplete;
	}
	
	private Element element;
	private String title;
	private String coverImageUrl;
	private String author;
	private String bookUrl;
	private String authorUrl;
	private String description;
	private String metadata;
	
	public ResultElement(Element element) {
		try {
			this.element = element;
			Element title = element.select(TITLE_SELECTOR).get(0);
			this.title = title.ownText();
			
		} catch (IndexOutOfBoundsException e) {
			// TODO(kodlee): Handle with Boot,log once merged with main branch
			throw e;
		}
		
	}
}
