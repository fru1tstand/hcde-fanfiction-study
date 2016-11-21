package me.fru1t.fanfiction.web.page;

import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import me.fru1t.fanfiction.web.page.element.StoryListElement;

public class StoryListPage {
	private static final String RESULT_ELEMENTS_SELECTOR =
			"#content_wrapper_inner .z-list.zhover.zpointer";

	private ArrayList<StoryListElement> storyListElements;

	public StoryListPage(String document) throws Exception {
		this.storyListElements = new ArrayList<>();
		
		Document doc = Jsoup.parse(document);
		
		for (Element element : doc.select(RESULT_ELEMENTS_SELECTOR)) {
			storyListElements.add(new StoryListElement(element));
		}
	}
	
	public ArrayList<StoryListElement> getStoryElements() {
		return storyListElements;
	}
}
