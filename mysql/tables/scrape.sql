CREATE TABLE `scrape` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `url` varchar(2000) NOT NULL,
   `content` mediumtext,
   PRIMARY KEY (`id`),
   KEY `fk_scrape_session_idx` (`session_id`),
   CONSTRAINT `fk_scrape_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
CREATE TABLE `scrape_review` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `url` varchar(2000) NOT NULL,
   `content` mediumtext,
   PRIMARY KEY (`id`),
   KEY `fk_scrape_review_session_idx` (`session_id`),
   CONSTRAINT `fk_scrape_review_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
 
 CREATE TABLE `scrape_playground` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `url` varchar(2000) NOT NULL,
   `content` mediumtext,
   PRIMARY KEY (`id`),
   KEY `fk_scrape_playground_session_idx` (`session_id`),
   CONSTRAINT `fk_scrape_playground_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;