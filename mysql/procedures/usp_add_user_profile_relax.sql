DELIMITER $$
CREATE PROCEDURE `usp_add_user_profile_relax`(
    in_ff_id int(11),
    in_user_name varchar(128),
    in_country_name VARCHAR(128),
	in_join_date int(10),
	in_update_date int(10),
	in_bio mediumtext,
	in_age tinyint(4),
	in_gender char(6)
)
BEGIN
	DECLARE v_user_id INT DEFAULT (SELECT fn_insfet_user(in_user_ff_id, in_user_name));
    DECLARE v_join_date INT DEFAULT IF(in_join_date > 0, in_join_date, NULL);
    DECLARE v_update_date INT DEFAULT IF(in_update_date > 0, in_update_date, NULL);
    DECLARE v_age INT DEFAULT IF(in_age > 0 AND in_age < 100, in_age, NULL);

	INSERT IGNORE INTO `user_profile_relax` 
					(`ff_id`, `user_id`, `user_name`,
					 `country_name`, `join_date`, `update_date`, 
					 `bio`, `age`, `gender` )
			 VALUES (in_ff_id, v_user_id, in_user_name,
					 in_country_name, v_join_date, v_update_date,
					 in_bio, v_age, in_gender);
END$$
DELIMITER ;
