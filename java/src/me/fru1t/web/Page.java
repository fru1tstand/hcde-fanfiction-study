package me.fru1t.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Page {
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
