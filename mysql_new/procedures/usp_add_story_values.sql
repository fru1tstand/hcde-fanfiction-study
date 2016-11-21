# Very simplified version of adding story meta-data,
# here, we ignore the newer version of story if the same ff_story_id exists

DELIMITER $$
CREATE PROCEDURE `usp_add_story_values`(
	-- story data (foreign keys)
    in_fandom_url VARCHAR(2000),
    in_user_ff_id INT,
    in_user_name VARCHAR(128),
    in_rating_name VARCHAR(45),
    in_language_name VARCHAR(128),
    
    -- story data 
    in_ff_story_id INT,
    in_title VARCHAR(256),
    in_chapters INT,
    in_words INT,
    in_reviews INT,
    in_favorites INT,
    in_followers INT,
    in_date_published INT(10),
    in_date_updated INT(10),
    in_is_complete TINYINT
    # OUT out_v_story_id INT
)
PROC:BEGIN
	-- set the foreign keys # These should not be null
    DECLARE v_fandom_id INT DEFAULT (SELECT `id` FROM `fandom` WHERE `url` = in_fandom_url);
    DECLARE v_user_id INT DEFAULT (SELECT fn_insfet_user(in_user_ff_id, in_user_name));
    DECLARE v_rating_id INT DEFAULT (SELECT fn_insfet_rating(in_rating_name));
    DECLARE v_language_id INT DEFAULT (SELECT fn_insfet_language(in_language_name));
    
    DECLARE story_id INT DEFAULT (SELECT `id` FROM `story` WHERE `ff_story_id` = in_ff_story_id);
    
    IF (story_id IS NULL) THEN
		INSERT INTO `story` (`fandom_id`, `user_id`, `rating_id`, `language_id`, 
									`ff_story_id`, `title`, `chapters`, `words`, `reviews`, 
									`favorites`, `followers`, `date_published`, `date_updated`, 
									`is_complete`)
							VALUES (v_fandom_id, v_user_id, v_rating_id, v_language_id, 
									in_ff_story_id, in_title, in_chapters, in_words, in_reviews, 
									in_favorites, in_followers, in_date_published, in_date_updated, 
									in_is_complete);
		# SET story_id = LAST_INSERT_ID();
	END IF;
	
    # SELECT in_ff_story_id AS ffStoryId, story_id AS storyId;
END$$

DELIMITER ;

