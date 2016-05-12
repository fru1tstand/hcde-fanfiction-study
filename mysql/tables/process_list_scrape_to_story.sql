CREATE TABLE `fanfiction`.`process_list_scrape_to_story` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '',
  `story_id` INT NOT NULL COMMENT '',
  `scrape_id` INT NOT NULL COMMENT '',
  `session_id` INT NOT NULL COMMENT '',
  `date` INT(10) NOT NULL COMMENT '',
  `meta_did_successfully_parse` TINYINT NULL DEFAULT 0 COMMENT '',
  `story_did_successfully_parse` TINYINT NULL DEFAULT 0 COMMENT '',
  `metadata` TEXT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `fk_process_list_scrape_to_story_story_idx` (`story_id` ASC)  COMMENT '',
  INDEX `fk_process_list_scrape_to_story_scrape_idx` (`scrape_id` ASC)  COMMENT '',
  INDEX `fk_process_list_scrape_to_story_session_idx` (`session_id` ASC)  COMMENT '',
  UNIQUE INDEX `uq_story_scrape_session` (`story_id` ASC, `scrape_id` ASC, `session_id` ASC)  COMMENT '',
  CONSTRAINT `fk_process_list_scrape_to_story_story`
    FOREIGN KEY (`story_id`)
    REFERENCES `fanfiction`.`story` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_process_list_scrape_to_story_scrape`
    FOREIGN KEY (`scrape_id`)
    REFERENCES `fanfiction`.`scrape` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `fk_process_list_scrape_to_story_session`
    FOREIGN KEY (`session_id`)
    REFERENCES `fanfiction`.`session` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE);
