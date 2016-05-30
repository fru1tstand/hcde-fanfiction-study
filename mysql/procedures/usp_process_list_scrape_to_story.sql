USE `fanfiction`;
DROP procedure IF EXISTS `usp_process_list_scrape_to_story`;

DELIMITER $$
USE `fanfiction`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_process_list_scrape_to_story`(
	-- story data
	in_category_name VARCHAR(128),
    in_fandom_name VARCHAR(128),
    in_fandom_url VARCHAR(2000),
    
    in_user_ff_id INT,
    in_user_name VARCHAR(128),
    in_rating_name VARCHAR(45),
    in_language_name VARCHAR(128),
    in_ff_story_id INT,
    in_title VARCHAR(256),
    in_chapters INT,
    in_words INT,
    in_reviews INT,
    in_favorites INT,
    in_followers INT,
    in_date_published INT(10),
    in_date_updated INT(10),
    in_is_complete TINYINT,
    
    -- scrape data
    in_scrape_id INT,
    
    -- process_list_scrape_to_story data
    in_session_name VARCHAR(128),
    in_process_date INT(10),
    in_meta_did_successfully_parse TINYINT,
    in_story_did_successfully_parse TINYINT,
    in_metadata TEXT
)
PROC:BEGIN
	-- Variable declaration
	DECLARE v_story_id INT;
    DECLARE v_last_update_scrape_date INT;
    DECLARE v_this_update_scrape_date INT;
    DECLARE v_fandom_id INT;
    DECLARE v_user_id INT;
    DECLARE v_rating_id INT;
    DECLARE v_language_id INT;
    DECLARE v_session_id INT;
    
    -- Preconditions
    IF (SELECT `id` FROM `scrape` WHERE `id` = in_scrape_id) IS NULL THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The given scrape id doesn\'t exist.';
    END IF;
    
    -- Set variables we will always use
    SET v_this_update_scrape_date = (SELECT `date` FROM `scrape` WHERE `id` = in_scrape_id);
    SET v_fandom_id = (SELECT fn_insfet_fandom(in_category_name, in_fandom_name, in_fandom_url));
    SET v_user_id = (SELECT fn_insfet_user(in_user_ff_id, in_user_name));
    SET v_rating_id = (SELECT fn_insfet_rating(in_rating_name));
    SET v_language_id = (SELECT fn_insfet_language(in_language_name));
    
	-- Check if story exists
    SET v_story_id = (SELECT `id` FROM `story` WHERE `ff_story_id` = in_ff_story_id);
    IF v_story_id IS NOT NULL THEN
		-- Story exists, we want to verify we have a newer version and update
        -- Get the date of scrape from the previous process.
        SET v_last_update_scrape_date = (
			SELECT MAX(`scrape`.`date`) FROM `process_list_scrape_to_story`
				INNER JOIN `story` ON `story`.`id` = `process_list_scrape_to_story`.`story_id`
                INNER JOIN `scrape` ON `scrape`.`id` = `process_list_scrape_to_story`.`scrape_id`
				WHERE `story`.`ff_story_id` = in_ff_story_id
		);
        -- Check how fresh our data is.
        IF v_last_update_scrape_date >= v_this_update_scrape_date THEN
			LEAVE PROC;
        END IF;
        
        -- Update
        UPDATE `story` SET 
			`fandom_id` = v_fandom_id,
            `user_id` = v_user_id,
            `rating_id` = v_rating_id,
            `language_id` = v_language_id,
            `title` = in_title,
            `chapters` = in_chapters,
            `words` = in_words,
            `reviews` = in_reviews,
            `favorites` = in_favorites,
            `followers` = in_followers,
            `date_published` = in_date_published,
            `date_updated` = in_date_updated,
            `is_complete` = in_is_complete
		WHERE `ff_story_id` = in_ff_story_id;
    ELSE
		-- Story doesn't exist, we want to insert
        INSERT INTO `story` (`fandom_id`, `user_id`, `rating_id`, `language_id`, `ff_story_id`, `title`, `chapters`, `words`, `reviews`, `favorites`, `followers`, `date_published`, `date_updated`, `is_complete`)
			VALUES (v_fandom_id, v_user_id, v_rating_id, v_language_id, in_ff_story_id, in_title, in_chapters, in_words, in_reviews, in_favorites, in_followers, in_date_published, in_date_updated, in_is_complete);
		SET v_story_id = LAST_INSERT_ID();
    END IF;
    
    -- Collect session id
    SET v_session_id = (SELECT fn_insfet_session(in_session_name));
    
    -- finally, insert into process_list_scrape_to_story
    INSERT INTO `process_list_scrape_to_story` (`story_id`, `scrape_id`, `session_id`, `date`, `meta_did_successfully_parse`, `story_did_successfully_parse`, `metadata`)
		VALUES (v_story_id, in_scrape_id, v_session_id, in_process_date, in_meta_did_successfully_parse, in_story_did_successfully_parse, in_metadata);
END$$

DELIMITER ;

