package me.fru1t.fanfiction.web.page;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import me.fru1t.fanfiction.web.page.element.ReviewElement;

public class ReviewListPage {
	private ArrayList<ReviewElement> reviewElements;
	private int contentLen;
	
	public ReviewListPage(int ffStoryId, int chapter, Document reviewListPageDoc, int sid) {
		reviewElements = new ArrayList<>();
		contentLen = 0;
		
		Elements reviewCells = reviewListPageDoc
				.select("div#content_wrapper_inner table td[style=\"padding-top:10px;padding-bottom:10px\"]");
		
		
		for (Element cell : reviewCells) { 
			if (cell.text().equals("No Reviews found.")) continue;
			
			int ff_id = -1; String name = null;
			int date = -1; String content = null;

			String href = cell.select("a").last().attr("href");
			
			String dateStr = cell.select("span").attr("data-xutime");
			if (dateStr != null && !dateStr.isEmpty()) date = sanitizeInteger(dateStr);
			content = cell.select("div[style=\"margin-top:5px\"]").text();
			contentLen += (content == null) ? 0 : content.length()*2;
				
			Matcher m = Pattern.compile("^/u/(?<ffId>[0-9]+)/(.*)$").matcher(href);
			if (m.matches()) {
				ff_id = sanitizeInteger(m.group("ffId"));
				name = cell.select("a").last().text();
			} else { // anonymous user
				for (Element tagElem : cell.select("*")) tagElem.remove();
				name = cell.text();
			}
			
			if (name.length() > 512) { // too long to fit into the DB; define this user as Guest
				name = "Guest";
			}
			
			ReviewElement relem = new ReviewElement(ffStoryId, chapter, ff_id, name, date, content);
			relem.setScrapeId(sid);
			reviewElements.add(relem);
		}
	}

	public ArrayList<ReviewElement> getReviewElements() {
		return this.reviewElements;
	}
	
	public int getContentLen() {
		return this.contentLen;
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