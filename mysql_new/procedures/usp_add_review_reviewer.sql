# 

DELIMITER $$
CREATE PROCEDURE `usp_add_review_reviewer`(
	-- foreign keys
    in_user_ff_id int(11),
    in_user_name VARCHAR(512),
    in_story_id int(11),
    
    -- data 
    in_date INT(10),
    in_chapter int(11),
    in_content MEDIUMTEXT
)
PROC:BEGIN
	-- set the foreign keys 
    -- user_id can be null if this person is anonymous
    DECLARE v_user_id INT DEFAULT in_user_ff_id;
    IF (v_user_id IS NOT NULL) THEN
		SET v_user_id = (SELECT fn_insfet_user(in_user_ff_id, in_user_name));
    END IF;
    
    INSERT INTO `review` (`user_id`, `story_id`, `date`, `chapter`, `content`)
		VALUES (v_user_id, in_story_id, in_date, in_chapter, in_content);
END$$

DELIMITER ;

