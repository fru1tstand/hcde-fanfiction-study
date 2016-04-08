CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_scrape_add_raw`(
	IN scrape_session_name VARCHAR(128),
    IN scrape_type_name VARCHAR(128),
    IN scrape_date INT,
    IN scrape_url VARCHAR(255),
    IN scrape_content MEDIUMTEXT
)
proc:BEGIN
	DECLARE scrape_session_id INT DEFAULT (SELECT `id` FROM `scrape_session` WHERE `name` = scrape_session_name);
    DECLARE scrape_type_id INT DEFAULT (SELECT `id` FROM `scrape_type` WHERE `name` = scrape_type_name);
    
    IF scrape_type_id IS NULL THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Scrape session name doesn\'t exist';
        LEAVE proc;
    END IF;
    
    IF scrape_session_id IS NULL THEN
		INSERT INTO `scrape_session` (`name`) VALUES (scrape_session_name);
        SET scrape_session_id = LAST_INSERT_ID();
    END IF;
    
    INSERT INTO `scrape_raw` (`scrape_session_id`, `scrape_type_id`, `date`, `url`, `content`)
		VALUES (scrape_session_id, scrape_type_id, scrape_date, scrape_url, scrape_content);
END