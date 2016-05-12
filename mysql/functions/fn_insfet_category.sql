USE `fanfiction`;
DROP function IF EXISTS `fn_insfet_category`;

DELIMITER $$
USE `fanfiction`$$
CREATE FUNCTION `fn_insfet_category` (name VARCHAR(128))
RETURNS INTEGER
BEGIN
	DECLARE ret_id INT DEFAULT (SELECT `id` FROM `category` WHERE `category`.`name` = name);
    IF (ret_id IS NULL) THEN
		INSERT INTO `category` (`name`) VALUES (name);
        RETURN LAST_INSERT_ID();
    END IF;
    RETURN ret_id;
END
$$

DELIMITER ;

