CREATE TABLE `fandom` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `category_id` int(11) NOT NULL,
   `name` varchar(128) NOT NULL,
   `url` varchar(2000) NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `name_UNIQUE` (`name`),
   UNIQUE KEY `uq_name_category_id` (`category_id`,`name`),
   KEY `fk_fandom_category_idx` (`category_id`),
   KEY `ix_name` (`name`),
   CONSTRAINT `fk_fandom_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8