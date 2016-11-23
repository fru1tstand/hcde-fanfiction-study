#

DELIMITER $$
CREATE FUNCTION `fn_insfet_user`(
	in_ff_id INT,
    in_user_name VARCHAR(128)
) RETURNS int(11)
BEGIN
	DECLARE v_user_id INT DEFAULT (SELECT `id` FROM `user` WHERE `ff_id` = in_ff_id);
    IF (v_user_id IS NULL) THEN
		INSERT INTO `user` (`ff_id`, `name`) VALUES (in_ff_id, in_user_name);
        RETURN LAST_INSERT_ID();
    END IF;
    RETURN v_user_id;
END$$

DELIMITER ;