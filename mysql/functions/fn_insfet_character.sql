DELIMITER $$
CREATE FUNCTION `fn_insfet_character` (lu_name VARCHAR(128))
RETURNS INTEGER
BEGIN
	DECLARE ret_id INT DEFAULT (SELECT `id` FROM `character` WHERE `character`.`name` = lu_name);
    IF (ret_id IS NULL) THEN
		INSERT INTO `character` (`name`) VALUES (lu_name);
        RETURN LAST_INSERT_ID();
    END IF;
    RETURN ret_id;
END$$

DELIMITER ;