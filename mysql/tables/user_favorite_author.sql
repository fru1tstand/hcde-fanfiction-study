CREATE TABLE `user_favorite_author_batch` (
  `ff_id` INT(11) NOT NULL,
  `favorite_ff_id` INT(11) NOT NULL,
  `favorite_name` varchar(128) NOT NULL,
  PRIMARY KEY (`ff_id`, `favorite_ff_id`),
  INDEX `fk_favorite_author_ff_idx` (`ff_id` ASC),
  INDEX `favorite_author_favorite_ff_idx` (`favorite_ff_id` ASC));

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

# For users, we will add favorite_ff_id not present in the current user table

# update user with not-present fav authors
INSERT INTO `user` (`ff_id`, `name`)
SELECT t1.favorite_ff_id, t1.favorite_name
  FROM `user_favorite_author_relax` t1
 WHERE t1.favorite_ff_id NOT IN (SELECT `ff_id`
								 FROM `user`);

INSERT INTO `user_favorite_author` (`user_id`, `favorite_user_id`)
SELECT t1.user_id, t2.user_id
  FROM `user_favorite_author_relax` t1 JOIN `user` t2 
		ON t1.favorite_ff_id = t2.ff_id;
        
# now go and redo this whole scraping, converting process for the 
# newly updated users in `user`!!!!!

                                 
