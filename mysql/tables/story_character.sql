CREATE TABLE `fanfiction`.`story_character` (
  `story_id` INT NOT NULL COMMENT '',
  `character_id` INT NOT NULL COMMENT '',
  PRIMARY KEY (`story_id`, `character_id`),
  INDEX `fk_story_character_story_idx` (`story_id` ASC)  COMMENT '',
  INDEX `fk_story_character_character_idx` (`character_id` ASC)  COMMENT '',
  UNIQUE INDEX `uq_story_character` (`story_id` ASC, `character_id` ASC)  COMMENT '',
  CONSTRAINT `fk_story_character_story`
    FOREIGN KEY (`story_id`)
    REFERENCES `fanfiction`.`story` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_story_character_character`
    FOREIGN KEY (`character_id`)
    REFERENCES `fanfiction`.`character` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE);
