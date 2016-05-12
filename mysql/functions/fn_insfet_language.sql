USE `fanfiction`;
DROP function IF EXISTS `fn_insfet_language`;

DELIMITER $$
USE `fanfiction`$$
CREATE FUNCTION `fn_insfet_language` (lu_name VARCHAR(128))
RETURNS INTEGER
BEGIN
	DECLARE ret_id INT DEFAULT (SELECT `id` FROM `language` WHERE `language`.`name` = lu_name);
    IF (ret_id IS NULL) THEN
		INSERT INTO `language` (`name`) VALUES (lu_name);
        RETURN LAST_INSERT_ID();
    END IF;
    RETURN ret_id;
END$$

DELIMITER ;

