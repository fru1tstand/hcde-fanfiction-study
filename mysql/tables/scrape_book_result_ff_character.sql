CREATE TABLE `scrape_book_result_ff_character` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ff_character_id` int(11) NOT NULL,
  `ff_book_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_character_book` (`ff_character_id`,`ff_book_id`),
  KEY `fk_scrape_book_result_ff_character_ff_character_idx` (`ff_character_id`),
  CONSTRAINT `fk_scrape_book_result_ff_character_ff_character` FOREIGN KEY (`ff_character_id`) REFERENCES `ff_character` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8