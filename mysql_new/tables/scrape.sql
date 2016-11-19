# SCRAPE_CATEGORY
CREATE TABLE `scrape_metadata` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `url` varchar(2000) NOT NULL,
   `content` mediumtext,
   `helping_id` int(11) DEFAULT NULL,  
   PRIMARY KEY (`id`),
   KEY `fk_scrape_metadata_session_idx` (`session_id`),
   CONSTRAINT `fk_scrape_metadata_session` 
   FOREIGN KEY (`session_id`) 
   REFERENCES `session` (`id`) 
		ON DELETE CASCADE
		ON UPDATE CASCADE
 ) ENGINE=InnoDB;