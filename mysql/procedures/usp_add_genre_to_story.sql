USE `fanfiction`;
DROP procedure IF EXISTS `usp_add_genre_to_story`;

DELIMITER $$
USE `fanfiction`$$
CREATE PROCEDURE `usp_add_genre_to_story` (
	ff_story_id INT,
    genre_name VARCHAR(128)
)
BEGIN
	DECLARE story_id INT DEFAULT (SELECT `id` FROM `story` WHERE `ff_story_id` = ff_story_id);
    DECLARE genre_id INT DEFAULT (SELECT fn_insfet_genre(genre_name));
    
    IF story_id IS NULL THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Failed to add genre to story. No story with given ff_story_id.';
    END IF;
    
    INSERT INTO `story_genre` (`story_id`, `genre_id`) VALUES (story_id, genre_id);
END
$$

DELIMITER ;

