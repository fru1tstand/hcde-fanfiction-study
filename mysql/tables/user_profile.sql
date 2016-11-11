CREATE TABLE `user_profile_relax` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `ff_id` int(11) NOT NULL,
  `user_id` INT NOT NULL,
  `user_name` VARCHAR(128) NOT NULL,
  `country_name` VARCHAR(128) NULL,
  `join_date` INT(10) NULL,
  `update_date` INT(10) NULL,
  `bio` MEDIUMTEXT NULL,
  `age` TINYINT NULL,
  `gender` CHAR(6) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `fk_user_profile_user_unique_idx` (`user_id` ASC),
  CONSTRAINT `fk_user_profile_user_unique`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);

CREATE TABLE `user_profile_strict` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `country_id` INT NULL,
  `join_date` INT(10) NULL,
  `update_date` INT(10) NULL,
  `bio` MEDIUMTEXT NULL,
  `age` TINYINT NULL,
  `gender` CHAR(6) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `fk_user_profile_user_unique_idx` (`user_id` ASC),
  INDEX `fk_user_profile_country_idx` (`country_id` ASC),
  CONSTRAINT `fk_user_profile_user_unique_idx`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user_profile_country`
    FOREIGN KEY (`country_id`)
    REFERENCES `country` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
    
