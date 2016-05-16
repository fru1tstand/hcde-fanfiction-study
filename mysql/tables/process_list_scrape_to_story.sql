CREATE TABLE `process_list_scrape_to_story` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `story_id` int(11) NOT NULL,
   `scrape_id` int(11) NOT NULL,
   `session_id` int(11) NOT NULL,
   `date` int(10) NOT NULL,
   `meta_did_successfully_parse` tinyint(4) DEFAULT '0',
   `story_did_successfully_parse` tinyint(4) DEFAULT '0',
   `metadata` text,
   PRIMARY KEY (`id`),
   KEY `fk_process_list_scrape_to_story_story_idx` (`story_id`),
   KEY `fk_process_list_scrape_to_story_scrape_idx` (`scrape_id`),
   KEY `fk_process_list_scrape_to_story_session_idx` (`session_id`),
   CONSTRAINT `fk_process_list_scrape_to_story_scrape` FOREIGN KEY (`scrape_id`) REFERENCES `scrape` (`id`) ON UPDATE CASCADE,
   CONSTRAINT `fk_process_list_scrape_to_story_session` FOREIGN KEY (`session_id`) REFERENCES `session` (`id`) ON UPDATE CASCADE,
   CONSTRAINT `fk_process_list_scrape_to_story_story` FOREIGN KEY (`story_id`) REFERENCES `story` (`id`) ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8