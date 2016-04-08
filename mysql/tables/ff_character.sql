CREATE TABLE `ff_character` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ff_real_book_id` int(11) NOT NULL,
  `name` varchar(128) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ff_real_book_id_name` (`ff_real_book_id`,`name`),
  KEY `fk_ff_character_ff_real_book_idx` (`ff_real_book_id`),
  CONSTRAINT `fk_ff_character_ff_real_book` FOREIGN KEY (`ff_real_book_id`) REFERENCES `ff_real_book` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8