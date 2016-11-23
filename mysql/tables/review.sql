CREATE TABLE `review` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_ff_id` INT(11) NULL,
  `user_name` varchar(128) NOT NULL,
  `date` INT(10) NULL,
  `ff_story_id` INT NOT NULL,
  `chapter` INT NOT NULL,
  `content` MEDIUMTEXT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_review_user_ff_idx` (`user_ff_id` ASC),
  INDEX `fk_review_ff_story_idx` (`ff_story_id` ASC));

CREATE TABLE `review_strict` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NOT NULL,
  `story_id` INT NOT NULL,
  `chapter` INT NOT NULL,
  `date` INT(10) NULL,
  `content` MEDIUMTEXT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_review_user_idx` (`user_id` ASC),
  INDEX `fk_review_story_idx` (`story_id` ASC),
  CONSTRAINT `fk_review_user_idx`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_review_story_idx`
    FOREIGN KEY (`story_id`)
    REFERENCES `story` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
    
# For users, we will add favorite_ff_id not present in the current user table

# update user with not-present fav authors
INSERT INTO `user` (`ff_id`, `name`)
SELECT t1.user_ff_id, t1.user_name
  FROM `review_relax` t1
 WHERE t1.user_ff_id NOT IN (SELECT `ff_id`
							   FROM `user`);

# now go and redo this whole scraping, converting process for the 
# newly updated users in `user`!!!!!


# For stories, we will delete the rows that have ff_story_id not in story table
# But this should not happen b/c we scraped reviews based on this story table...
INSERT INTO `review` (`user_id`, `story_id`, `chapter`, `date`, `content`)
SELECT t3.user_id, t4.id, t3.chapter, t3.date, t3.content
  FROM (SELECT  t2.id as `user_id`, 
				t1.ff_story_id as `ff_story_id`, 
				t1.chapter as `chapter`, 
				t1.date as `date`,
				t1.content as `content`
		  FROM `review_relax` t1 JOIN `user` t2 ON t1.user_ff_id = t2.ff_id) 
			t3 JOIN `story` t4 ON t3.ff_story_id = t4.ff_story_id;