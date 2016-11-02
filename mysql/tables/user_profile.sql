CREATE TABLE `user_profile` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `country_id` INT NOT NULL,
  `join_date` INT(10) NULL,
  `update_date` INT(10) NULL,
  `bio` MEDIUMTEXT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_user_profile_user_idx` (`user_id` ASC),
  INDEX `fk_user_profile_country_idx` (`country_id` ASC),
  CONSTRAINT `fk_user_profile_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user_profile_country`
    FOREIGN KEY (`country_id`)
    REFERENCES `country` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
