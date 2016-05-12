USE `fanfiction`;
DROP procedure IF EXISTS `usp_add_character_to_story`;

DELIMITER $$
USE `fanfiction`$$
CREATE PROCEDURE `usp_add_character_to_story` (
	ff_story_id INT,
    character_name VARCHAR(128)
)
BEGIN
	DECLARE story_id INT DEFAULT (SELECT `id` FROM `story` WHERE `ff_story_id` = ff_story_id);
    DECLARE character_id INT DEFAULT (SELECT fn_insfet_character(character_name));
    
    IF story_id IS NULL THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Failed to add character to story. No story with given ff_story_id.';
    END IF;
    
    INSERT INTO `story_character` (`story_id`, `character_id`) VALUES (story_id, character_id);
END
$$

DELIMITER ;

