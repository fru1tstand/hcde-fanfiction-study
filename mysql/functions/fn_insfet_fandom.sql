USE `fanfiction`;
DROP function IF EXISTS `fn_insfet_fandom`;

DELIMITER $$
USE `fanfiction`$$
CREATE DEFINER=`root`@`localhost` FUNCTION `fn_insfet_fandom`(
	in_category_name VARCHAR(128),
    in_fandom_name VARCHAR(128),
    in_fandom_url VARCHAR(1000)
) RETURNS int(11)
BEGIN
    DECLARE v_fandom_id INT DEFAULT (SELECT `id` FROM `fandom` WHERE `name` = in_fandom_name);
    DECLARE v_category_id INT;
    
    IF (v_fandom_id IS NULL) THEN
		 SET v_category_id = (SELECT fn_insfet_category(in_category_name));
         INSERT INTO `fandom` (`category_id`, `name`, `url`) VALUES (v_category_id, in_fandom_name, in_fandom_url);
         RETURN LAST_INSERT_ID();
    END IF;
    
    RETURN v_fandom_id;
END$$

DELIMITER ;

