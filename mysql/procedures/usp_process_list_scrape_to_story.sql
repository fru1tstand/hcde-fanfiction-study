USE `fanfiction`;
DROP procedure IF EXISTS `usp_process_list_scrape_to_story`;

DELIMITER $$
USE `fanfiction`$$
	-- story data
CREATE PROCEDURE `usp_process_list_scrape_to_story` (
	category_name VARCHAR(128),
    fandom_name VARCHAR(128),
    user_ff_id INT,
    user_name VARCHAR(128),
    rating_name VARCHAR(45),
    language_name VARCHAR(128),
    ff_story_id INT,
    title VARCHAR(256),
    chapters INT,
    words INT,
    reviews INT,
    favorites INT,
    followers INT,
    date_published INT(10),
    date_updated INT(10),
    is_complete TINYINT,
    
    -- scrape data
    scrape_id INT,
    
    -- process_list_scrape_to_story data
    session_name VARCHAR(128),
    process_date INT(10),
    meta_did_successfully_parse TINYINT,
    story_did_successfully_parse TINYINT,
    metadata TEXT
)
PROC:BEGIN
	-- Variable declaration
	DECLARE story_id INT;
    DECLARE last_update_scrape_date INT;
    DECLARE this_update_scrape_date INT;
    DECLARE fandom_id INT;
    DECLARE user_id INT;
    DECLARE rating_id INT;
    DECLARE language_id INT;
    DECLARE session_id INT;
    
    -- Preconditions
    IF (SELECT `id` FROM `scrape` WHERE `id` = scrape_id) IS NULL THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The given scrape id doesn\'t exist.';
    END IF;
    
    -- Set variables we will always use
    SET this_update_scrape_date = (SELECT `date` FROM `scrape` WHERE `id` = scrape_id);
    SET fandom_id = (SELECT fn_insfet_fandom(category_name, fandom_name));
    SET user_id = (SELECT fn_insfet_user(user_ff_id, user_name));
    SET rating_id = (SELECT fn_insfet_rating(rating_name));
    SET language_id = (SELECT fn_insfet_language(language_name));
    
	-- Check if story exists
    SET story_id = (SELECT `id` FROM `story` WHERE `ff_story_id` = ff_story_id);
    IF story_id IS NOT NULL THEN
		-- Story exists, we want to verify we have a newer version and update
        -- Get the date of scrape from the previous process.
        SET last_update_scrape_date = (
			SELECT MAX(`scrape`.`date`) FROM `process_list_scrape_to_story`
				INNER JOIN `story` ON `story`.`id` = `process_list_scrape_to_story`.`story_id`
                INNER JOIN `scrape` ON `scrape`.`id` = `process_list_scrape_to_story`.`scrape_id`
				WHERE `story`.`ff_story_id` = ff_story_id
		);
        -- Check how fresh our data is.
        IF last_update_scrape_date > this_update_scrape_date THEN
			LEAVE PROC;
        END IF;
        
        -- Update
        UPDATE `story` SET 
			`fandom_id` = fandom_id,
            `user_id` = user_id,
            `rating_id` = rating_id,
            `language_id` = language_id,
            `title` = title,
            `chapters` = chapters,
            `words` = words,
            `reviews` = reviews,
            `favorites` = favorites,
            `followers` = followers,
            `date_published` = date_published,
            `date_updated` = date_updated,
            `is_complete` = is_complete
		WHERE `ff_story_id` = ff_story_id;
    ELSE
		-- Story doesn't exist, we want to insert
        INSERT INTO `story` (`fandom_id`, `user_id`, `rating_id`, `language_id`, `ff_story_id`, `title`, `chapters`, `words`, `reviews`, `favorites`, `followers`, `date_published`, `date_updated`, `is_complete`)
			VALUES (fandom_id, user_id, rating_id, language_id, ff_story_id, title, chapters, words, reviews, favorites, followers, date_published, date_updated, is_complete);
		SET story_id = LAST_INSERT_ID();
    END IF;
    
    -- Collect session id
    SET session_id = (SELECT fn_insfet_session(session_name));
    
    -- finally, insert into process_list_scrape_to_story
    INSERT INTO `process_list_scrape_to_story` (`story_id`, `scrape_id`, `session_id`, `date`, `meta_did_successfully_parse`, `story_did_successfully_parse`, `metadata`)
		VALUES (story_id, scrape_id, session_id, process_date, meta_did_successfully_parse, story_did_successfully_parse, metadata);
END
$$

DELIMITER ;

