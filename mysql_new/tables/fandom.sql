# CONVERT_CATEGORY
CREATE TABLE `fandom` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `category_id` int(11) NOT NULL,
   `name` varchar(128) NOT NULL,
   `url` varchar(2000) NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `category_id_name_UNIQUE` (`category_id`, `name`),
   KEY `fk_fandom_category_idx` (`category_id`),
   KEY `ix_name` (`name`),
   CONSTRAINT `fk_fandom_category` 
   FOREIGN KEY (`category_id`) 
   REFERENCES `category` (`id`) 
		ON UPDATE CASCADE
		ON DELETE CASCADE
 ) ENGINE=InnoDB;
 