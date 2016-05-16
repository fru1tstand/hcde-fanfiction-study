USE `fanfiction`;
DROP procedure IF EXISTS `usp_add_character_to_story`;

DELIMITER $$
USE `fanfiction`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_add_character_to_story`(
	in_ff_story_id INT,
    in_character_name VARCHAR(128)
)
BEGIN
	DECLARE v_story_id INT DEFAULT (SELECT `id` FROM `story` WHERE `ff_story_id` = in_ff_story_id);
    DECLARE v_character_id INT DEFAULT (SELECT fn_insfet_character(in_character_name));
    
    IF v_story_id IS NULL THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Failed to add character to story. No story with given ff_story_id.';
    END IF;
    
    INSERT INTO `story_character` (`story_id`, `character_id`) VALUES (v_story_id, v_character_id)
		ON DUPLICATE KEY UPDATE `story_id` = `story_id`;
END$$

DELIMITER ;

