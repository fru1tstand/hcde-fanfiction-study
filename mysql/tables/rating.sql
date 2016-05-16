CREATE TABLE `rating` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `name` varchar(45) NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `name_UNIQUE` (`name`),
   KEY `ix_name` (`name`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8