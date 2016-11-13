CREATE TABLE `user_favorite_story_relax` (
  `ff_id` INT(11) NOT NULL,
  `ff_story_id` INT NOT NULL,
  PRIMARY KEY (`ff_id`, `ff_story_id`),
  INDEX `user_favorite_story_ff_story_idx` (`ff_story_id` ASC));

CREATE TABLE `user_favorite_story` (
  `user_id` INT(11) NOT NULL,
  `story_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `story_id`),
  INDEX `fk_user_favorite_story_story_idx` (`story_id` ASC),
  CONSTRAINT `fk_user_favorite_story_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user_favorite_story_story`
    FOREIGN KEY (`story_id`)
    REFERENCES `story` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
    

# For stories, we will delete the rows that have ff_story_id not in story table
#INSERT INTO `user_favorite_story` (`user_id`, `story_id`)
SELECT distinct ff_story_id FROM 
(SELECT t1.user_id, t1.ff_story_id, t2.id
  FROM `user_favorite_story_relax` t1 LEFT JOIN `story` t2
	   ON t1.ff_story_id = t2.ff_story_id) t3 WHERE id IS NULL;

SELECT * FROM `user_favorite_story_relax`;
DELETE FROM `user_favorite_story` where user_id > 0;
SELECT * FROM `user_favorite_story`;