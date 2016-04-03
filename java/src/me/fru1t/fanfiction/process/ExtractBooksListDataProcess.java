package me.fru1t.fanfiction.process;

import java.util.List;
import java.util.regex.Matcher;

import me.fru1t.fanfiction.Boot;
import me.fru1t.fanfiction.Database;
import me.fru1t.fanfiction.web.page.BookSearchPage;
import me.fru1t.fanfiction.web.page.element.BookResultElement;

public class ExtractBooksListDataProcess implements Runnable {
	
	@Override
	public void run() {
		String document = Database.getRandomBookSearchResult();
		BookSearchPage bsp = new BookSearchPage(document);
		List<BookResultElement> elements = bsp.getBookResultElements();
		for (BookResultElement element : elements) {
			Boot.log(element.metadata);
			Boot.log(element.processedMetadata.toString());
		}
//		Boot.log(elements.get(0).processedMetadata.toString());
	}

}
