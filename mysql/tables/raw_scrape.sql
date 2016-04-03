CREATE TABLE `raw_scrape` (
  `raw_scrape_id` int(11) NOT NULL AUTO_INCREMENT,
  `session_id` varchar(128) NOT NULL,
  `date` int(10) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `content` mediumtext,
  PRIMARY KEY (`raw_scrape_id`,`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8