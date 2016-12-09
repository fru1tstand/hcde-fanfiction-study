package me.fru1t.fanfiction.web.page.element;

import me.fru1t.util.FFElement;

public class ReviewElement extends FFElement {
	public int ff_story_id;
	public int chapter;
	public int reviewer_ff_id;
	public String reviewer_name;
	public int review_date;
	public String content;

	public ReviewElement(int fsi, int chap, int id, String name, int date, String content) {
		this.ff_story_id = fsi;
		this.chapter = chap;
		this.reviewer_ff_id = id;
		this.reviewer_name = name;
		this.review_date = date;
		this.content = content;
	}
	
	public ReviewElement(ReviewElement copy) {
		this.ff_story_id = copy.ff_story_id;
		this.chapter = copy.chapter;
		this.reviewer_ff_id = copy.reviewer_ff_id;
		this.reviewer_name = copy.reviewer_name;
		this.review_date = copy.review_date;
		this.content = copy.content;
		this.setScrapeId(copy.getScrapeId());
	}
}