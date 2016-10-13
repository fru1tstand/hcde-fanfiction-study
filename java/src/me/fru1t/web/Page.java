package me.fru1t.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Page {
	public static final String LINK_HREF_ATTR = "href";

	protected Document document;

	public Page(String document) {
		this(Jsoup.parse(document));
	}

	public Page(Document document) {
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}
}
