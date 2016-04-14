package me.fru1t.fanfiction.web.page;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.web.page.element.BookResultElement;

public class BookSearchPage {
	private static final String RESULT_ELEMENTS_SELECTOR = 
			"#content_wrapper_inner .z-list.zhover.zpointer";
	private static final String REAL_BOOK_NAME_PREV_ELEMENT_SELECT =
			".xcontrast_txt.icon-chevron-right.xicon-section-arrow";
	
	private Document document;
	private List<BookResultElement> resultElements;
	private String realBookName;
	
	public BookSearchPage(String document) {
		this(Jsoup.parse(document));
	}
	
	public BookSearchPage(Document document) {
		this.document = document;
		this.resultElements = new ArrayList<>();
		try {
			this.realBookName = document
				.select(REAL_BOOK_NAME_PREV_ELEMENT_SELECT)
				.get(0)
				.nextSibling()
				.toString();
		} catch (IndexOutOfBoundsException e) {
			Boot.getLogger().log(e, "Couldn't find the book name from this search page");
		}
		for (Element element : document.select(RESULT_ELEMENTS_SELECTOR)) {
			resultElements.add(new BookResultElement(this.realBookName, element));
		}
	}
	
	public List<BookResultElement> getBookResultElements() {
		return resultElements;
	}
	
	public Document getDocument() {
		return document;
	}
}
