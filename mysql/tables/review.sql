CREATE TABLE `review` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `story_id` INT NOT NULL,
  `chapter` INT NOT NULL,
  `user_id` INT(11) NULL,
  `date` INT(10) NULL,
  `content` MEDIUMTEXT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_review_story_idx` (`story_id` ASC),
  CONSTRAINT `fk_review_story`
    FOREIGN KEY (`story_id`)
    REFERENCES `story` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
