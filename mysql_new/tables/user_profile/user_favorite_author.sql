#

CREATE TABLE `user_favorite_author` (
  `user_id` INT(11) NOT NULL,
  `favorite_user_id` INT(11) NOT NULL,
  PRIMARY KEY (`user_id`, `favorite_user_id`),
  INDEX `fk_favorite_author_favorite_user_idx` (`favorite_user_id` ASC),
  CONSTRAINT `fk_favorite_author_user`
    FOREIGN KEY (`user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_favorite_author_favorite_user`
    FOREIGN KEY (`favorite_user_id`)
    REFERENCES `user` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE);
                  
CREATE TABLE `user_favorite_author_dummy` (
  `user_id` INT(11) NOT NULL,
  `favorite_ff_id` INT(11) NOT NULL,
  `favorite_name` varchar(512) NOT NULL,
  PRIMARY KEY (`user_id`, `favorite_ff_id`),
  INDEX `fk_favorite_author_favorite_ff_idx` (`favorite_ff_id` ASC));
