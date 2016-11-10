DELIMITER $$
CREATE PROCEDURE `usp_add_user_fav_story_strict`(
    in_my_ff_id int(11),
    in_story_id int(11)
)
BEGIN
	DECLARE v_user_id INT DEFAULT (SELECT `id` from `user` where `ff_id` = in_my_ff_id);
	DECLARE v_story_id INT DEFAULT (SELECT `id` from `story` where `ff_story_id` = in_story_id);
	
    IF v_user_id IS NULL OR v_story_id IS NULL THEN
		INSERT INTO `null_favstory` (`ff_id`, `story_id`)
			VALUES (in_my_ff_id, in_story_id);
	ELSE
        INSERT INTO `user_favorite_story` (`user_id`, `story_id`)
			VALUES (v_user_id, v_story_id);
	END IF;
END$$
DELIMITER ;