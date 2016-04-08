CREATE TABLE `scrape_book_result_ff_genre` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ff_genre_id` int(11) DEFAULT NULL,
  `ff_book_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ff_genre_id_ff_book_id` (`ff_genre_id`,`ff_book_id`),
  CONSTRAINT `fk_scrape_book_result_ff_genre_ff_genre` FOREIGN KEY (`ff_genre_id`) REFERENCES `ff_genre` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8