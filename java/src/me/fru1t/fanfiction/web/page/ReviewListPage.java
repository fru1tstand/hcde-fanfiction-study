package me.fru1t.fanfiction.web.page;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ReviewListPage {

	
	public static class ReviewElement {
		public int reviewer_ff_id;
		public String reviewer_name;
		public int review_date;
		public String content;
		public ReviewElement(int id, String name, int date, String content) {
			this.reviewer_ff_id = id;
			this.reviewer_name = name;
			this.review_date = date;
			this.content = content;
		}
	}
	
	private List<ReviewElement> reviewElements;
	
	public ReviewListPage(Document reviewListPageDoc) {
		reviewElements = new ArrayList<>();
		Elements reviewCells = reviewListPageDoc
				.select("div#content_wrapper_inner table td[style=\"padding-top:10px;padding-bottom:10px\"]");

		for (Element cell : reviewCells) { 
			String href = cell.select("a").last().attr("href");
			Matcher m = Pattern.compile("^/u/(?<ffId>[0-9]+)/(.*)$").matcher(href);
			if (m.matches()) {
				int ff_id = sanitizeInteger(m.group("ffId"));
				String name = cell.select("a").last().text();
				int date = sanitizeInteger(cell.select("span").attr("data-xutime"));
				String content = cell.select("div[style=\"margin-top:5px\"]").text();
				
				reviewElements.add(new ReviewElement(ff_id, name, date, content));
			}
		}
	}

	public List<ReviewElement> getReviewElements() {
		return this.reviewElements;
	}
	
	/**
	 * Converts a string integer into an int type, stripping all non-numeric characters;
	 * specifically, commas.
	 */
	private static int sanitizeInteger(String s) {
		Matcher intMatcher = Pattern.compile("[^0-9]").matcher(s);
		return Integer.parseInt(intMatcher.replaceAll(""));
	}
	
	
}