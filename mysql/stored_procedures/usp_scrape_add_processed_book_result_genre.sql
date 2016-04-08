CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_scrape_add_processed_book_result_genre`(
	IN genre_name VARCHAR(128),
    IN ff_book_id INT
)
BEGIN
	DECLARE genre_id INT DEFAULT (SELECT `id` FROM `ff_genre` WHERE `name` = genre_name);
    
    IF genre_id IS NUll THEN
		INSERT INTO `ff_genre` (`name`) VALUES (genre_name);
        SET genre_id = LAST_INSERT_ID();
    END IF;
    
    INSERT IGNORE INTO `scrape_book_result_ff_genre` (`ff_genre_id`, `ff_book_id`) VALUES (genre_id, ff_book_id);
END