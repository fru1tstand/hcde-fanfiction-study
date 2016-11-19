
# CONVERT_FANDOM
CREATE TABLE `story` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   
   `fandom_id` int(11) NOT NULL,
   `user_id` int(11) NOT NULL,
   `rating_id` int(11) NOT NULL,
   `language_id` int(11) NOT NULL,
   
   `ff_story_id` int(11) NOT NULL,
   `title` varchar(256) NOT NULL,
   `chapters` int(11) DEFAULT '1',
   `words` int(11) DEFAULT '0',
   `reviews` int(11) DEFAULT '0',
   `favorites` int(11) DEFAULT '0',
   `followers` int(11) DEFAULT '0',
   `date_published` int(10) DEFAULT NULL,
   `date_updated` int(10) DEFAULT NULL,
   `is_complete` tinyint(1) DEFAULT '0',
   
   PRIMARY KEY (`id`),
   UNIQUE KEY `ff_story_id_UNIQUE` (`ff_story_id`),
   KEY `fk_story_fandom_idx` (`fandom_id`),
   KEY `fk_story_user_idx` (`user_id`),
   KEY `fk_story_rating_idx` (`rating_id`),
   KEY `fk_story_language_idx` (`language_id`),
   CONSTRAINT `fk_story_fandom` FOREIGN KEY (`fandom_id`) REFERENCES `fandom` (`id`) ON UPDATE CASCADE,
   CONSTRAINT `fk_story_language` FOREIGN KEY (`language_id`) REFERENCES `language` (`id`) ON UPDATE CASCADE,
   CONSTRAINT `fk_story_rating` FOREIGN KEY (`rating_id`) REFERENCES `rating` (`id`) ON UPDATE CASCADE,
   CONSTRAINT `fk_story_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON UPDATE CASCADE
 ) ENGINE=InnoDB;
 