CREATE TABLE `scrape_raw` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `scrape_session_id` int(11) NOT NULL,
  `date` int(10) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `content` mediumtext,
  PRIMARY KEY (`id`),
  KEY `fk_scrape_raw_scrape_session_idx` (`scrape_session_id`),
  CONSTRAINT `fk_scrape_raw_scrape_session` FOREIGN KEY (`scrape_session_id`) REFERENCES `scrape_session` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8