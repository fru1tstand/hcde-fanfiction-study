package me.fru1t.fanfiction.web.page;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.fru1t.fanfiction.web.page.element.BookResultElement;

public class BookSearchPage {
	private static final String RESULT_ELEMENTS_SELECTOR = 
			"#content_wrapper_inner .z-list.zhover.zpointer";
	
	private Document document;
	private List<BookResultElement> resultElements;
	
	public BookSearchPage(String document) {
		this(Jsoup.parse(document));
	}
	
	public BookSearchPage(Document document) {
		this.document = document;
		this.resultElements = new ArrayList<>();
		for (Element element : document.select(RESULT_ELEMENTS_SELECTOR)) {
			resultElements.add(new BookResultElement(element));
		}
	}
	
	public List<BookResultElement> getBookResultElements() {
		return resultElements;
	}
	
	public Document getDocument() {
		return document;
	}
}
