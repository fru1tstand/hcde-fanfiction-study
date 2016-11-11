CREATE TABLE `user_favorite_story_relax` (
  `user_id` INT(11) NOT NULL,
  `ff_story_id` INT NOT NULL,
  PRIMARY KEY (`user_id`, `ff_story_id`),
  INDEX `fk_user_favorite_story_ff_story_idx` (`ff_story_id` ASC));

ALTER TABLE `user_favorite_story_relax`
ADD CONSTRAINT `fk_user_favorite_story_story`
    FOREIGN KEY (`story_id`)
    REFERENCES `story` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE;


CREATE TABLE `user_favorite_story_strict` (
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

