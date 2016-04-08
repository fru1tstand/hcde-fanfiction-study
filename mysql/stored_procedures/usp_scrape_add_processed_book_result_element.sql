CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_scrape_add_processed_book_result_element`(
	IN scrape_raw_id INT,
    IN scrape_process_session_name VARCHAR(128),
    IN ff_real_book_name VARCHAR(128),
    IN date_processed int(11),
    IN ff_book_id int(11),
	IN ff_author_id int(11),
	IN book_title varchar(256),
	IN book_url varchar(1024),
	IN cover_image_url varchar(1024),
	IN cover_image_original_url varchar(1024),
	IN author varchar(64),
	IN author_url varchar(1024),
	IN synopsis varchar(2048),
	IN metadata varchar(1024),
	IN meta_rating varchar(2),
	IN meta_language varchar(45),
	IN meta_chapters int(11),
	IN meta_words int(11),
	IN meta_reviews int(11),
	IN meta_favorites int(11),
	IN meta_followers int(11),
	IN meta_date_updated int(11),
	IN meta_date_published int(11),
	IN meta_genres varchar(512),
	IN meta_characters varchar(512),
	IN meta_is_complete tinyint(4),
	IN meta_did_successfully_parse tinyint(4),
	IN did_successfully_parse tinyint(4)
)
BEGIN
	DECLARE scrape_process_session_id INT DEFAULT (SELECT `id` FROM `scrape_process_session` WHERE `name` = scrape_process_session_name);
    DECLARE ff_real_book_id INT DEFAULT (SELECT `id` FROM `ff_real_book` WHERE `name` = ff_real_book_name);
    
    IF scrape_process_session_id IS NULL THEN
		INSERT INTO `scrape_process_session` (`name`) VALUES (scrape_process_session_name);
        SET scrape_process_session_id = LAST_INSERT_ID();
    END IF;
    
    IF ff_real_book_Id IS NULL THEN
		INSERT INTO `ff_real_book` (`name`) VALUES (ff_real_book_name);
        SET ff_real_book_id = LAST_INSERT_ID();
    END IF;
    
    INSERT INTO `scrape_book_result_element` (
		`scrape_raw_id`,
		`scrape_process_session_id`,
        `ff_real_book_id`,
		`date_processed`,
		`ff_book_id`,
		`ff_author_id`,
		`book_title`,
		`book_url`,
		`cover_image_url`,
		`cover_image_original_url`,
		`author`,
		`author_url`,
		`synopsis`,
		`metadata`,
		`meta_rating`,
		`meta_language`,
		`meta_chapters`,
		`meta_words`,
		`meta_reviews`,
		`meta_favorites`,
		`meta_followers`,
		`meta_date_updated`,
		`meta_date_published`,
		`meta_genres`,
		`meta_characters`,
		`meta_is_complete`,
		`meta_did_successfully_parse`,
		`did_successfully_parse`) VALUES (
        scrape_raw_id,
		scrape_process_session_id,
        ff_real_book_id,
		date_processed,
		ff_book_id,
		ff_author_id,
		book_title,
		book_url,
		cover_image_url,
		cover_image_original_url,
		author,
		author_url,
		synopsis,
		metadata,
		meta_rating,
		meta_language,
		meta_chapters,
		meta_words,
		meta_reviews,
		meta_favorites,
		meta_followers,
		meta_date_updated,
		meta_date_published,
		meta_genres,
		meta_characters,
		meta_is_complete,
		meta_did_successfully_parse,
		did_successfully_parse);
END