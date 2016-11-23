# SCRAPE_CATEGORY
CREATE TABLE `category` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `name` varchar(128) NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `category_name_UNIQUE` (`name`),
   KEY `ix_name` (`name`)
) ENGINE=InnoDB;