DELIMITER $$
CREATE PROCEDURE `usp_add_user_profile_strict`(
    in_my_ff_id int(11),
    in_country_name VARCHAR(128),
	in_join_date int(10),
	in_update_date int(10),
	in_bio mediumtext,
	in_age tinyint(4),
	in_gender char(6)
)
BEGIN
    DECLARE v_user_id INT DEFAULT (SELECT `id` FROM `user` where ff_id = in_my_ff_id);
	DECLARE v_country_id INT DEFAULT (SELECT fn_insfet_country(in_country_name));
    DECLARE v_join_date INT DEFAULT IF(in_join_date > 0, in_join_date, NULL);
    DECLARE v_update_date INT DEFAULT IF(in_update_date > 0, in_update_date, NULL);
    DECLARE v_age INT DEFAULT IF(in_age > 0 AND in_age < 100, in_age, NULL);

	IF v_user_id IS NULL THEN 
		INSERT IGNORE INTO `null_user` (`ff_id`, `name`)
			VALUES (in_my_ff_id, "");
	ELSE
		INSERT INTO `user_profile` (`user_id`, `country_id`, `join_date`, `update_date`, 
									`bio`, `age`, `gender`)
				VALUES (v_user_id, v_country_id, v_join_date, v_update_date,
						in_bio, v_age, in_gender);
	END IF;
END$$
DELIMITER ;
