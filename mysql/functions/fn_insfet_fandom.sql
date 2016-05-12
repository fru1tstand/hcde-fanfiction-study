USE `fanfiction`;
DROP function IF EXISTS `fn_insfet_fandom`;

DELIMITER $$
USE `fanfiction`$$
CREATE FUNCTION `fn_insfet_fandom` (
	category_name VARCHAR(128),
    fandom_name VARCHAR(128)
)
RETURNS INTEGER
BEGIN
    DECLARE fandom_id INT DEFAULT (SELECT `id` FROM `fandom` WHERE `name` = fandom_name);
    DECLARE category_id INT;
    
    IF (fandom_id IS NULL) THEN
		 SET category_id = (SELECT fn_insfet_category(category_name));
         INSERT INTO `fandom` (`category_id`, `name`) VALUES (category_id, fandom_name);
         RETURN LAST_INSERT_ID();
    END IF;
    
    RETURN fandom_id;
END
$$

DELIMITER ;

