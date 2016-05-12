CREATE TABLE `fanfiction`.`fandom` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '',
  `category_id` INT NOT NULL COMMENT '',
  `name` VARCHAR(128) NOT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  UNIQUE INDEX `name_UNIQUE` (`name` ASC)  COMMENT '',
  INDEX `fk_fandom_category_idx` (`category_id` ASC)  COMMENT '',
  UNIQUE INDEX `uq_name_category_id` (`category_id` ASC, `name` ASC)  COMMENT '',
  CONSTRAINT `fk_fandom_category`
    FOREIGN KEY (`category_id`)
    REFERENCES `fanfiction`.`category` (`id`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE);
