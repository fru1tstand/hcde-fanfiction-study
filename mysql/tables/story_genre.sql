CREATE TABLE `fanfiction`.`story_genre` (
  `story_id` INT(11) NOT NULL COMMENT '',
  `genre_id` INT NOT NULL COMMENT '',
  PRIMARY KEY (`story_id`, `genre_id`)  COMMENT '',
  INDEX `fk_story_genre_genre_idx` (`genre_id` ASC)  COMMENT '',
  CONSTRAINT `fk_story_genre_story`
    FOREIGN KEY (`story_id`)
    REFERENCES `fanfiction`.`story` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_story_genre_genre`
    FOREIGN KEY (`genre_id`)
    REFERENCES `fanfiction`.`genre` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE);
