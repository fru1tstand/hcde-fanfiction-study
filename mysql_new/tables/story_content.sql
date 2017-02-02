
# CONVERT_FANDOM
CREATE TABLE `story_content` (
   `story_id` int(11) NOT NULL,
   `chapter` 
   int(11) NOT NULL,
   `chapter_title` varchar(256),
   `content` mediumtext,
   
   PRIMARY KEY (`story_id`, `chapter`),
   INDEX `fk_story_idx` (`story_id` ASC),
   CONSTRAINT `fk_story_idx` 
	FOREIGN KEY (`story_id`) 
	REFERENCES `story` (`id`) 
	ON UPDATE CASCADE
    ON DELETE CASCADE
 ) ENGINE=InnoDB;
 