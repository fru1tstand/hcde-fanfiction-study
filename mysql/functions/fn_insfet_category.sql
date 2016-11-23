DELIMITER $$
CREATE FUNCTION `fn_insfet_category`(in_category_name VARCHAR(128)) RETURNS int(11)
BEGIN
	DECLARE v_category_id INT DEFAULT (SELECT `id` FROM `category` WHERE `category`.`name` = in_category_name);
    IF (v_category_id IS NULL) THEN
		INSERT INTO `category` (`name`) VALUES (in_category_name);
        RETURN LAST_INSERT_ID();
    END IF;
    RETURN v_category_id;
END$$
DELIMITER ;