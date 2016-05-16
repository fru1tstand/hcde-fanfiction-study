CREATE TABLE `session` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `name` varchar(128) NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `session_name_UNIQUE` (`name`),
   KEY `ix_name` (`name`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8