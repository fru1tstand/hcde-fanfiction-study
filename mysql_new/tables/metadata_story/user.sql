#

CREATE TABLE `user` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `ff_id` int(11) NOT NULL,
   `name` varchar(512) NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `ff_id_UNIQUE` (`ff_id`),
   KEY `ix_name` (`name`),
   KEY `ix_ff_id` (`ff_id`)
 ) ENGINE=InnoDB