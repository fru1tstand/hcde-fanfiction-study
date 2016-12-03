#

DELIMITER $$
CREATE FUNCTION `fn_insfet_location`(
    in_location_name VARCHAR(128)
) RETURNS int(11)
BEGIN
	DECLARE v_location_id INT DEFAULT (SELECT `id` FROM `location` WHERE `name` = in_location_name);
    IF (v_location_id IS NULL) THEN
		INSERT INTO `location` (`name`) VALUES (in_location_name);
        RETURN LAST_INSERT_ID();
    END IF;
    RETURN v_location_id;
END$$

DELIMITER ;