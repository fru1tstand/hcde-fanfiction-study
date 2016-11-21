#

DELIMITER $$
CREATE PROCEDURE `usp_add_character_to_story`(
	in_story_id INT,
    in_character_name VARCHAR(128)
)
BEGIN
	DECLARE v_character_id INT DEFAULT (SELECT fn_insfet_character(in_character_name));
    
    INSERT INTO `story_character` (`story_id`, `character_id`) VALUES (in_story_id, v_character_id)
		ON DUPLICATE KEY UPDATE `story_id` = `story_id`;
END$$

DELIMITER ;

