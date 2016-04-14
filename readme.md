# Distributed Mentoring
Does distributed mentoring have a positive effect on writing ability as demonstrated by several attributes and writing quality?

###### Screenshots
Scraped Data (April 8th, 2016)  
![alt text](http://i.imgur.com/TQfSkux.png "Example of scraped data")  
  
Overall Statistics (April 8th, 2016)  
![alt text](http://i.imgur.com/oL8fWfU.png "Overall statistics")  
  
Genre-blocked statistics (April 8th, 2016)  
![alt text](http://i.imgur.com/VLpbKkD.png "Genre-blocked statistics")

## Worklog & Changelist
#### April 13, 2016 ~ April 22, 2016
###### Goals

1. Scrape select story content.
2. (Stretch) Process story content into easy to access metrics.
3. (Stretch) Process story content using easily implemented [readability tests](https://en.wikipedia.org/wiki/Readability_test).

###### Overview

+ Creation of worklog/changelist (this file).
+ Re-processed the `meta_chapters` column in `scrape_book_result_element`.  
  Due to a bug in the initial processing code, if the genre was omitted from within the metadata, the processing of the number of chapters would fail and default to "-1". 

###### Technical

+ Abstracted logging from Boot into `me.fru1t.util/Logger.java`.
+ Abstracted thread-safe, type-safe database polling into `me.fru1t.util.concurrent/DatabaseProducer.java`.


#### April 1, 2016 ~ April 8, 2016
###### Goals

1. Scrape book list data. ✓
2. Create easily found statistical generalizations from data. ✓

###### Overview

+ Created database structure to store crawled and processed data.
+ Laid out base program structure and added 3rd party libraries.
  + [Jsoup](http://jsoup.org/) for HTML processing.
  + [Connector/J](https://dev.mysql.com/downloads/connector/j/) for MySQL interfacing.
  + [JDT Annotations for Enhanced Null Analysis](http://mvnrepository.com/artifact/org.eclipse.jdt/org.eclipse.jdt.annotation) For Eclipse null analysis.
+ Created stored logging within repository to share process runs.
+ Successfully ran book list scrape process (+2.3GB / +18k rows / ~54hrs)
+ Successfully ran scrape processing (+272MB / +449k rows / ~2hrs)

###### Technical

+ Created FanFiction crawler.  
  Note the site is gzipped from the server and must be decoded.
+ Created base scrape and supporting lookup tables:
  + `ff_character` Contains the name of all characters used within stories.
  + `ff_genre` Contains all genres from FanFiction.
  + `ff_real_book` Contains all real stories (as opposed to fan fiction stories).
  + `scrape_book_result_element` Contains each book result from a scraped story search page by book.
  + `scrape_book_result_ff_character` Connects `scrape_book_result_element` and `ff_character`.
  + `scrape_book_result_ff_genre` Connects `scrape_book_result_element` and `ff_genre`.
  + `scrape_process_session` Contains the session names for processes.
  + `scrape_raw` Contains all scrapes.
  + `scrape_session` Contains the session names for scrapes.
  + `scrape_type` Contains the scrape type (eg. book search, book chapter, etc).
+ Created basic `scrape_book_result_element` min/max/avg aggregate views on words, reviews, favorites, follows, and chapters:
  + `author_statistics` Blocks by author and contains number of stories, genres, and characters used.
  + `genre_statistics` Blocks by genre and contains number of stories, and authors.
  + `overall_statistics` No blocking.
+ Added scrape processing and decomposition of loosely structured metadata via regex
