CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_scrape_add_processed_book_result_character`(
	IN real_book_name VARCHAR(128),
    IN character_name VARCHAR(128),
    IN ff_book_id INT(11)
)
BEGIN
	DECLARE character_id INT DEFAULT (
		SELECT `ff_character`.`id` FROM `ff_character`
		INNER JOIN `ff_real_book` ON `ff_real_book`.`id` = `ff_character`.`ff_real_book_id`
        WHERE `ff_character`.`name` = character_name
			AND `ff_real_book`.`name` = real_book_name);
	DECLARE ff_real_book_id INT DEFAULT (SELECT `id` FROM `ff_real_book` WHERE `name` = real_book_name);
    
    IF ff_real_book_id IS NULL THEN
		INSERT INTO `ff_real_book` (`name`) VALUES (real_book_name);
        SET ff_real_book_id = LAST_INSERT_ID();
    END IF;
	
    IF character_id IS NULL THEN
		INSERT INTO `ff_character` (`ff_real_book_id`, `name`) VALUES (ff_real_book_id, character_name);
        SET character_id = LAST_INSERT_ID();
    END IF;
    
    INSERT IGNORE INTO `scrape_book_result_ff_character` (`ff_character_id`, `ff_book_id`) VALUES (character_id, ff_book_id);
END