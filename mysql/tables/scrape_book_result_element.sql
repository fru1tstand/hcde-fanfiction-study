CREATE TABLE `scrape_book_result_element` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `scrape_session_id` int(11) NOT NULL,
  `date_processed` int(11) NOT NULL,
  `ff_book_id` int(11) DEFAULT NULL,
  `ff_author_id` int(11) DEFAULT NULL,
  `book_title` varchar(256) DEFAULT NULL,
  `book_url` varchar(1024) DEFAULT NULL,
  `cover_image_url` varchar(1024) DEFAULT NULL,
  `cover_image_original_url` varchar(1024) DEFAULT NULL,
  `author` varchar(64) DEFAULT NULL,
  `author_url` varchar(1024) DEFAULT NULL,
  `synopsis` varchar(2048) DEFAULT NULL,
  `metadata` varchar(1024) DEFAULT NULL,
  `meta_rating` varchar(2) DEFAULT NULL,
  `meta_language` varchar(45) DEFAULT NULL,
  `meta_chapters` int(11) DEFAULT NULL,
  `meta_words` int(11) DEFAULT NULL,
  `meta_reviews` int(11) DEFAULT NULL,
  `meta_favorites` int(11) DEFAULT NULL,
  `meta_followers` int(11) DEFAULT NULL,
  `meta_date_updated` int(11) DEFAULT NULL,
  `meta_date_published` int(11) DEFAULT NULL,
  `meta_genres` varchar(512) DEFAULT NULL,
  `meta_characters` varchar(512) DEFAULT NULL,
  `meta_is_complete` tinyint(4) DEFAULT NULL,
  `meta_did_successfully_parse` tinyint(4) DEFAULT NULL,
  `did_successfully_parse` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_scrape_book_result_element_scrape_session_idx` (`scrape_session_id`),
  CONSTRAINT `fk_scrape_book_result_element_scrape_session_id` FOREIGN KEY (`scrape_session_id`) REFERENCES `scrape_session` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8