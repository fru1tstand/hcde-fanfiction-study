# Distributed Mentoring
Does distributed mentoring have a positive effect on writing ability as demonstrated by several attributes and writing quality?
[Click here](https://github.com/fru1tstand/hcde-fanfiction-study/archive/master.zip) to download the latest version of this repository (along with the workbooks, source code, etc).

## Worklog & Changelist
### May 30, 2016 ~ June 9, 2016
###### Goals
0. Increase throughput of scraping process via increased IP allocation (using IPv6). ✓
  + FanFiction.net does not support IPv6.
1. Scrape & Process all category pages. ✓
2. Scrape & Process all fandom (story list) pages. ✓
3. Start building of heatmap (genre distribution) application.

###### Changelist
+ Removed unsorted logs from repo.
+ Abstracted converter process.
+ Added 'url' column to `fandom` table and subsequent functions and stored procedures.
+ Created and ran `scrape/CategoryPage` and `convert/CategoryToFandoms` successfully adding all fandoms to the database.
+ Added debugging logic for easier testing on larger url scrapes.
+ Attempted to implement IPv6 support, but found out that FanFiction.net has no AAAA (IPv6) DNS record, meaning we can't scrape the site using our IPv6 addresses.
+ Added a minimum content length parameter to the scraper as a fail-safe method for non-erroring returned requests.
+ Added Sessions enum to track session pragmatically.
+ Abstracted database connectivity through a custom `DatabaseConnectionPool` interface which allows for automatical retrying, reconnecting, and resolving of issues with less boilerplate code.
+ Updated all `Producer`s to utilize the new `DatabaseConnectionPool` interface.
+ Heavily modified FandomScrape to automatically calculate maximum number of pages within itself and scrape all.
+ Renamed a couple classes for consistency.
+ Added more User-Agent headers in MultiIPScraper.
+ Parallel processing update.
  + `MultiIPCrawler` now only supports asynchronous fetching of pages.
  + Rewrote `ScrapeProcess`, `FandomProducer`, and all related submodules to conform to new concurrent paradigm.
  + Disabled non-conforming code for old processes.
+ Successfully scraped all fandom pages.
+ Created and successfully ran `convert/FandomToStories`


### May 23, 2016 ~ May 27, 2016
###### Goals
0. Scraped remaining Harry Potter (Rated M) story list metadata. ✓
1. Scrape all of English Harry Potter story content.
  + This was determined to be infeasible as we'd be attempting to scrape over 2 million pages.
2. Scrape all of FanFiction.net Metadata.

###### Changelist
+ Revamped database interface to a pooled resource with automated fail-safe execution plan.
  + Updated all processes to conform to new system.
+ Removed processes and associated files that were single-use.
+ Revamped scraping interface through singleton instance.
+ Fixed `ThreadUtils` normal distribution for wait times.
+ Added `url` column to `Fandom` table.
+ Abstracted scrape process to reduce redundancy in fail-safe code.

### May 16, 2016 ~ May 19, 2016
###### Goals
1. Scrape all of My Little Pony, Harry Potter, and Dr. Who. ✓
  + Process all scrapes for component metadata. ✓
2. Create Reviews vs Review Ranking graph in Tableau. ✓
3. (Stretch) Scrape all My Little Pony, Harry Potter, and Dr. Who author profiles.
  + Process all scrapes for profile information including join date.
4. (Stretch) Create Reviews per Story vs # of days author has been on FanFiction.net.
5. (Stretch) Work on genre heat map data.

###### Screenshots
All graphs use data collected only from Harry Potter, My Little Pony, and Dr. Who.  
  
  
Most Reviews to Least Reviews per Story.  
![alt text](http://i.imgur.com/WUfRT7J.png "Most Reviews to Least Reviews per Story for Harry Potter, Dr. Who, and My Little Pony.")

Average # of Reviews per Date Updated by Month.  
![alt text](http://i.imgur.com/9pWV4hL.png "Average # of Reviews per Date Updated by Month.")

Number of Stories per Date Updated by Month.  
![alt text](http://i.imgur.com/1R1WN2U.png "Number of Stories per Date Updated by Month.")

Number of Reviews per Chapter per Story, from Most to Least.  
![alt text](http://i.imgur.com/LDNDfpb.png "Number of Reviews per Chapter per Story, from Most to Least.")

Chapter vs Favorites for each Story.  
![alt text](http://i.imgur.com/XSc7I0H.png "Chapter vs Favorites for each Story.")

###### Changelist
+ Attempting to pinpoint and remove possible memory leaks.
  + `StoredProcedures` now always explicitly closes all SQL statements.
+ Cleaned up/Renamed files according to agreed upon naming conventions.
  + `RawScrapeProducer` -> `ScrapeProducer`
  + `ExtractBooksListDataProcess` -> (Removed)
  + `ScrapeBookPageFromCategoriesProcess` -> `ScrapeBooksListProcess`
  + `BookSearchPage` -> `FandomStoryListPage`
  + `BookResultElement` -> `FandomStoryListElement`
+ Improved internal tooling.
  + `MultiIPCrawler` now self-regulates crawl intervals and it thread-safe.
+ Reworked `BookResultElement` to process metadata in a cleaner, more strategic way which is less prone to anomaly errors.
+ Modified naming conventions in database to fix bugs caused by columns, variables, and parameters being named the same.
+ Added strategic indexing in database to improve throughput.


### May 2, 2016 ~ May 12, 2016
###### Goals
1. Extract story content from raw scrape data.
2. Process story content to analyze with various easy to implement metrics.
  + Word count (per chapter, per sentence).
  + Simple [readability tests](https://en.wikipedia.org/wiki/Readability_test) (per chapter).
3. Scrape top 100 stories (ordered by number of reviews) for top 13 fandoms (ordered by number of stories) for all categories (excluding msc). ✓
4. Create heatmap of genres from the 100 stories for every fandom.
5. (Stretch) Create compiled tool on server for scrape jobs (server/client GUI tool).

###### Overview
+ Freak accident week -- Hooray... Hard drive with database crashes and remains unreadable.
+ Database structure recovered (with no data) on new hard drive.
  + Simplified relationships and removed many circular references.
  + Normalized all tables.
  + Created insert or fetch (insfet) functions for all look up tables (LUTs).
  + Created more rugged stored procedures for inserting data.
+ Integrated new database structure into back end.
  + Organized database-interacting code files better.
  + Deprecated all DatabaseProducer and Processes (I'm deferring the fixing of these files until they're used again).
+ Created process to scrape top 100 stories.
  + Successfully ran with 680 book list pages from 136 fandom pages scraped.

###### Technical
+ Removed all old tables for shorter, cleaner names: `category`, `character`, `fandom`, `genre`, `language`, `process_list_scrape_to_story`, `rating`, `scrape`, `session`, `story`, `story_character`, `story_genre`, `user`
+ Added insfet functions for `category`, `character`, `fandom`, `genre`, `language`, `rating`, `session`, `user`.
+ Re-added insert usps `add_character_to_story`, `add_genre_to_story`, `add_scrape`, `process_list_scrape_to_story`.
+ `usp_scrape_add_processed_book_result_element` replaced with `process_list_scrape_to_story` which more rigorously checks and keeps the most up-to-date information.
+ `ScrapeBookPageFromCategoriesProcess` used for the 100 stories from 13 fandoms on all categories.


### April 25, 2016 ~ April 29, 2016
###### Goals
1. (Potentially) Scrape more Harry Potter stories. ✓
2. Extract story content from raw scrape data.
3. Process story content to analyze with various easy to implement metrics. 
  + Word count (per chapter, per sentence).
  + Simple [readability tests](https://en.wikipedia.org/wiki/Readability_test) (per chapter).
4. Create a comparative spreadsheet (ratings, reviews, chapters, words, etc) of the top 13 sources for each category (-msc) ✓
5. Create heatmap/spreadsheet for genres most paired together ✓
6. (Stretch) Start scraping profile pages
7. (Stretch) Global metrics for all stories ✓

###### Overview
+ Created [Category vs Source Material](https://github.com/fru1tstand/hcde-fanfiction-study/blob/master/spreadsheets/Category%20vs%20Source%20Material.xlsx) spreadsheet.
+ Running the scraper for book chapters process produced an error most likely caused by a drop in internet connection, but has since been restarted.
+ Created processes for generating genre heat map data.
+ Completed process run for genre heat map data generation and created [genre heat map](https://github.com/fru1tstand/hcde-fanfiction-study/blob/master/spreadsheets/Genre%20Heatmap.xlsx).

###### Screenshots
Genre Heat Map (April 28th, 2016)  
![alt text](http://i.imgur.com/AfAbwQ2.png "Genre Heatmap")

First 13 Story's Words for all Categories Combined (April 28th, 2016)  
![alt text](http://i.imgur.com/aDLrwHE.png "First 13 Story's Words for all Categories")

Fandoms per Source per Category  
![alt text](http://i.imgur.com/ssEOS4o.png "Fandoms per Source per Category")

###### Technical
+ Modified `me.fru1t.fanfiction.process/ScrapeBookChaptersProcess` to continue scraping HP stories
+ Running `ScrapeBookChaptersProcess` on remote server produced `com.mysql.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException: No operations allowed after connection closed.` after 30 hours of operation.
  + Process was resumed with backtracking.
+ Created `java/src/me/fru1t/fanfiction/database/schema/scrape/StoryForHeatMapProducer.java` and `java/src/me/fru1t/fanfiction/process/GenreHeatMapValueProcess.java` to generate heatmap data.


### April 11, 2016 ~ April 22, 2016
###### Goals

1. Scrape select story content. ✓
2. (Stretch) Process story content into easy to access metrics.
3. (Stretch) Process story content using easily implemented [readability tests](https://en.wikipedia.org/wiki/Readability_test).

###### Overview
+ Creation of worklog/changelist (this file).
+ Successfully re-parsed `metadata` in `scrape_book_result_element` to fix `meta_chapters` (Δ49k rows / ~3mins).  
  Due to a bug in the initial processing code, if the genre was omitted from within the metadata, the processing of the number of chapters would fail and default to "-1".
+ Added [Apache HttpComponents](https://hc.apache.org/) to handle multi-ip scraping.
+ Created book chapter scrape process.
+ Successfully ran book chapter scrape process (+6.5GB / +59k rows / ~54hrs)

###### Technical
+ Abstracted logging from Boot into `me.fru1t.util/Logger.java`.
+ Abstracted thread-safe, type-safe database polling into `me.fru1t.util.concurrent/DatabaseProducer.java`.
+ Created `me.fru1t.fanfiction.process/FixMetadataProcess` script to fix `meta_chapters` in `scrape_book_result_element`.
+ Created `me.fru1t.fanfiction.process/ScrapeBookChaptersProcess` to scrape book chapters.
+ Ran `me.fru1t.fanfiction.process/ScrapeBookChaptersProcess` with the following issues:
  + Wait time calculation was never calculated per ip address used. Fixed by diving total wait time with the number of available IPs.
  + InvocationTargetException triggered when a possible negative wait time was given. Fixed by `MAX`ing wait times with 200ms.


### March 28, 2016 ~ April 8, 2016
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

###### Screenshots
Scraped Data (April 8th, 2016)  
![alt text](http://i.imgur.com/TQfSkux.png "Example of scraped data")  
  
Overall Statistics (April 8th, 2016)  
![alt text](http://i.imgur.com/oL8fWfU.png "Overall statistics")  
  
Genre-blocked statistics (April 8th, 2016)  
![alt text](http://i.imgur.com/VLpbKkD.png "Genre-blocked statistics")

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
