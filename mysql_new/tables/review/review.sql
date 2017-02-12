# 
CREATE TABLE `review` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT(11) NULL, # anonymous user will be null
  `story_id` INT NOT NULL,
  
  `date` INT(10) DEFAULT '-1',
  `chapter` INT NOT NULL,
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
