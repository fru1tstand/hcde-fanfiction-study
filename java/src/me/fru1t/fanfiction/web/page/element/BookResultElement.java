package me.fru1t.fanfiction.web.page.element;

import org.jsoup.nodes.Element;

import me.fru1t.fanfiction.Boot;

public class BookResultElement {
	private static final String TITLE_SELECTOR = "a.stitle";
	private static final String COVER_IMAGE_SELECTOR = "a.stitle img";
	private static final String AUTHOR_SELECTOR = "a[href^=\"/u/\"]";
	private static final String SYNOPSIS_SELECTOR = ".z-indent.z-padtop";
	private static final String METADATA_SELECTOR = ".z-indent.z-padtop .z-padtop2.xgray";
	
	public class ProcessedData {
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
	private String bookTitle;
	private String bookUrl;
	private String coverImageUrl;
	private String author;
	private String authorUrl;
	private String synopsis;
	private String metadata;
	private ProcessedData processedData;
	
	private boolean hasSuccessfullyProcessed;
	
	public BookResultElement(Element result) {
		this.hasSuccessfullyProcessed = false;
		this.element = result;
		
		try {
			Element title = result.select(TITLE_SELECTOR).get(0);
			this.bookTitle = title.ownText();
			this.bookUrl = title.attr("href");
			
			this.coverImageUrl = result.select(COVER_IMAGE_SELECTOR).get(0).attr("src");
			
			Element author = result.select(AUTHOR_SELECTOR).get(0);
			this.author = author.ownText();
			this.authorUrl = author.attr("href");
			
			this.synopsis = result.select(SYNOPSIS_SELECTOR).get(0).ownText();
			
			this.metadata = result.select(METADATA_SELECTOR).get(0).text();
			
			this.hasSuccessfullyProcessed = true;
		} catch (IndexOutOfBoundsException e) {
			Boot.log(e, "A required element wasn't found on the page.");
		}
	}

	public Element getElement() {
		return element;
	}

	public String getBookTitle() {
		return bookTitle;
	}

	public String getBookUrl() {
		return bookUrl;
	}

	public String getCoverImageUrl() {
		return coverImageUrl;
	}

	public String getAuthor() {
		return author;
	}

	public String getAuthorUrl() {
		return authorUrl;
	}

	public String getSynopsis() {
		return synopsis;
	}

	public String getMetadata() {
		return metadata;
	}

	public ProcessedData getProcessedData() {
		return processedData;
	}

	public boolean hasSuccessfullyProcessed() {
		return hasSuccessfullyProcessed;
	}
}
