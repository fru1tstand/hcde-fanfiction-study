#

CREATE TABLE `user_profile` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `location_id` INT NULL,
  `join_date` INT(10) DEFAULT -1,
  `update_date` INT(10) DEFAULT -1,
  `bio` MEDIUMTEXT NULL,
  `age` TINYINT NULL,
  `gender` CHAR(6) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `fk_user_profile_user_unique_idx` (`user_id` ASC),
  INDEX `fk_user_profile_location_idx` (`location_id` ASC),
  CONSTRAINT `fk_user_profile_user_unique_idx`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_user_profile_location`
    FOREIGN KEY (`location_id`)
    REFERENCES `location` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
    
