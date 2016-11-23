
DELIMITER $$
CREATE PROCEDURE `usp_add_user_fav_author_strict`(
    in_my_ff_id int(11),
    in_user_name VARCHAR(128),
    in_favorite_user_id int(11),
    in_favorite_name VARCHAR(128)
)
BEGIN
	DECLARE v_user_id INT DEFAULT (SELECT `id` from `user` where `ff_id` = in_my_ff_id);
    DECLARE v_favorite_user_id INT DEFAULT (SELECT `id` from `user` where `ff_id` = in_favorite_user_id);
    
	IF v_user_id IS NULL THEN 
		INSERT IGNORE INTO `null_user` (`ff_id`, `name`)
			VALUES (in_my_ff_id, in_user_name);
    ELSEIF v_favorite_user_id IS NULL THEN
		INSERT IGNORE INTO `null_favorite_author` (`ff_id`, `name`)
			VALUES (in_favorite_user_id, in_favorite_name);
	ELSE
        INSERT INTO `user_favorite_author` (`user_id`, `favorite_user_id`)
			VALUES (v_user_id, v_favorite_user_id);
    END IF;

END$$
DELIMITER ;