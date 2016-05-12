USE `fanfiction`;
DROP function IF EXISTS `fn_insfet_session`;

DELIMITER $$
USE `fanfiction`$$
CREATE FUNCTION `fn_insfet_session` (lu_name VARCHAR(128))
RETURNS INTEGER
BEGIN
	DECLARE ret_id INT DEFAULT (SELECT `id` FROM `session` WHERE `session`.`name` = lu_name);
    IF (ret_id IS NULL) THEN
		INSERT INTO `session` (`name`) VALUES (lu_name);
        RETURN LAST_INSERT_ID();
    END IF;
    RETURN ret_id;
END$$

DELIMITER ;

