package me.fru1t.fanfiction.process;

import java.util.List;

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
			System.out.printf("Book: %s; Author: %s;\r\n", element.getBookTitle(), element.getAuthor());
		}
	}

}
