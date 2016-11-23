#

DELIMITER $$
CREATE PROCEDURE `usp_add_genre_to_story`(
	in_story_id INT,
    in_genre_name VARCHAR(128)
)
BEGIN
	DECLARE v_genre_id INT DEFAULT (SELECT fn_insfet_genre(in_genre_name));
    INSERT INTO `story_genre` (`story_id`, `genre_id`) VALUES (in_story_id, v_genre_id)
		ON DUPLICATE KEY UPDATE `story_id` = `story_id`;
END$$

DELIMITER ;

