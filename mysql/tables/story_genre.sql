CREATE TABLE `story_genre` (
   `story_id` int(11) NOT NULL,
   `genre_id` int(11) NOT NULL,
   KEY `fk_story_genre_story_idx` (`story_id`),
   KEY `fk_story_genre_genre_idx` (`genre_id`),
   CONSTRAINT `fk_story_genre_genre` FOREIGN KEY (`genre_id`) REFERENCES `genre` (`id`) ON UPDATE CASCADE,
   CONSTRAINT `fk_story_genre_story` FOREIGN KEY (`story_id`) REFERENCES `story` (`id`) ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8