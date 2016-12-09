# 

DELIMITER $$
CREATE PROCEDURE `usp_add_user_and_user_profile`(
	in_ff_id INT,
    in_user_name VARCHAR(512),
	in_location_name VARCHAR(128),
	in_join_date INT(10),
	in_update_date INT(10),
	in_bio MEDIUMTEXT,
	in_age TINYINT,
	in_gender CHAR(6)
)
PROC:BEGIN
	DECLARE v_user_id INT DEFAULT (SELECT fn_insfet_user(in_ff_id, in_user_name));
	DECLARE v_location_id INT DEFAULT NULL;
    IF (in_location_name IS NOT NULL) THEN
		SET v_location_id = (SELECT fn_insfet_location(in_location_name));
	END IF;

    INSERT INTO `user_profile` (`user_id`, `location_id`, `join_date`, `update_date`, `bio`, `age`, `gender`)
		VALUES (v_user_id, v_location_id, in_join_date, in_update_date, in_bio, in_age, in_gender);
END$$

DELIMITER ;

