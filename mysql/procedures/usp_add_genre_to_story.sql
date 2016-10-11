
DELIMITER $$
CREATE PROCEDURE `usp_add_genre_to_story`(
	in_ff_story_id INT,
    in_genre_name VARCHAR(128)
)
BEGIN
	DECLARE v_story_id INT DEFAULT (SELECT `id` FROM `story` WHERE `ff_story_id` = in_ff_story_id);
    DECLARE v_genre_id INT DEFAULT (SELECT fn_insfet_genre(in_genre_name));
    
    IF v_story_id IS NULL THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Failed to add genre to story. No story with given ff_story_id.';
    END IF;
    
    INSERT INTO `story_genre` (`story_id`, `genre_id`) VALUES (v_story_id, v_genre_id)
		ON DUPLICATE KEY UPDATE `story_id` = `story_id`;
END$$

DELIMITER ;

