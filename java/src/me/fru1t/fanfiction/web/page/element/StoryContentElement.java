package me.fru1t.fanfiction.web.page.element;


import org.jsoup.nodes.Document;

import me.fru1t.util.FFElement;

public class StoryContentElement extends FFElement {
	
	public int ff_story_id;
	public int chapter;
	public String title;
	public String content;
	
	public StoryContentElement(Document storyPageDoc, int ff_story_id, int chapter) {
		this.ff_story_id = ff_story_id;
		this.chapter = chapter;
		
		// title
		if (storyPageDoc.select("SELECT#chap_select option[selected]").isEmpty())
			title = null;
		else
			title = storyPageDoc.select("SELECT#chap_select option[selected]").first().text();
		
		// content
		if (storyPageDoc.select("div#storytext").isEmpty())
			content = null;
		else
			content = storyPageDoc.select("div#storytext").html();
	}
	
	public int getContentLen() {
		return content == null ? 0 : content.length();
	}
}