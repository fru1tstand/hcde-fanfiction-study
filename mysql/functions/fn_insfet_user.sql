USE `fanfiction`;
DROP function IF EXISTS `fn_insfet_user`;

DELIMITER $$
USE `fanfiction`$$
CREATE FUNCTION `fn_insfet_user` (
	ff_id INT,
    user_name VARCHAR(128)
)
RETURNS INTEGER
BEGIN
	DECLARE user_id INT DEFAULT (SELECT `id` FROM `user` WHERE `ff_id` = ff_id);
    IF (user_id IS NULL) THEN
		INSERT INTO `user` (`ff_id`, `name`) VALUES (ff_id, user_name);
        RETURN LAST_INSERT_ID();
    END IF;
    RETURN user_id;
END
$$

DELIMITER ;

