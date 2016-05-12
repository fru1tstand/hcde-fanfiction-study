CREATE TABLE `fanfiction`.`scrape` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '',
  `session_id` INT(11) NOT NULL COMMENT '',
  `date` INT(10) NOT NULL COMMENT '',
  `url` VARCHAR(255) NOT NULL COMMENT '',
  `content` MEDIUMTEXT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `fk_scrape_session_idx` (`session_id` ASC)  COMMENT '',
  UNIQUE INDEX `uq_session_url` (`session_id` ASC, `url` ASC)  COMMENT '',
  CONSTRAINT `fk_scrape_session`
    FOREIGN KEY (`session_id`)
    REFERENCES `fanfiction`.`session` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE);
