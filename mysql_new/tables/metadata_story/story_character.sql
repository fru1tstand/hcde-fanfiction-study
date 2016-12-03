#

CREATE TABLE `story_character` (
   `story_id` int(11) NOT NULL,
   `character_id` int(11) NOT NULL,
   PRIMARY KEY (`story_id`,`character_id`),
   KEY `fk_story_character_story_idx` (`story_id`),
   KEY `fk_story_character_character_idx` (`character_id`),
   CONSTRAINT `fk_story_character_character`
		FOREIGN KEY (`character_id`) 
        REFERENCES `character` (`id`) 
        ON UPDATE CASCADE
        ON DELETE CASCADE,
   CONSTRAINT `fk_story_character_story` 
		FOREIGN KEY (`story_id`) 
        REFERENCES `story` (`id`) 
        ON UPDATE CASCADE
        ON DELETE CASCADE
 ) ENGINE=InnoDB 