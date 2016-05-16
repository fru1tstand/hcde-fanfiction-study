CREATE TABLE `scrape` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `url` varchar(255) NOT NULL,
   `content` mediumtext,
   PRIMARY KEY (`id`),
   UNIQUE KEY `uq_session_url` (`session_id`,`url`),
   KEY `fk_scrape_session_idx` (`session_id`),
   CONSTRAINT `fk_scrape_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8