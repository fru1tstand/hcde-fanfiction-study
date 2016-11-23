# SCRAPE_CATEGORY

# DEMO : change `SUFFIX` to whatever that fit e.g. metadata, user, review, story ...
CREATE TABLE `scrape_[SUFFIX]` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `url` varchar(2000) NOT NULL,
   `content` mediumtext,  
   PRIMARY KEY (`id`),
   KEY `fk_scrape_[SUFFIX]_session_idx` (`session_id`),
   CONSTRAINT `fk_scrape_[SUFFIX]_session` 
   FOREIGN KEY (`session_id`) 
   REFERENCES `session` (`id`) 
		ON DELETE CASCADE
		ON UPDATE CASCADE
 ) ENGINE=InnoDB;
 
CREATE TABLE `scrape_user` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `url` varchar(2000) NOT NULL,
   `content` mediumtext,
   PRIMARY KEY (`id`),
   KEY `fk_scrape_user_session_idx` (`session_id`),
   CONSTRAINT `fk_scrape_user_session` 
   FOREIGN KEY (`session_id`) 
   REFERENCES `session` (`id`) 
		ON DELETE CASCADE
		ON UPDATE CASCADE
 ) ENGINE=InnoDB;
 
 CREATE TABLE `scrape_review` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `url` varchar(2000) NOT NULL,
   `content` mediumtext,
   PRIMARY KEY (`id`),
   KEY `fk_scrape_review_session_idx` (`session_id`),
   CONSTRAINT `fk_scrape_review_session` 
   FOREIGN KEY (`session_id`) 
   REFERENCES `session` (`id`) 
		ON DELETE CASCADE
		ON UPDATE CASCADE
 ) ENGINE=InnoDB;