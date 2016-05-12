package me.fru1t.fanfiction.web.page;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import me.fru1t.web.Page;

public class CategoryPage extends Page {
	private static final String FANDOM_LINKS_SELECTOR = "#list_output table tr td div a";
	
	public CategoryPage(Document document) {
		super(document);
	}
	
	public CategoryPage(String document) {
		super(document);
	}
	
	public Elements getFandomLinks() {
		return document.select(FANDOM_LINKS_SELECTOR);
	}
}
