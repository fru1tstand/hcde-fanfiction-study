CREATE TABLE `fanfiction`.`user` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '',
  `ff_id` INT NOT NULL COMMENT '',
  `name` VARCHAR(128) NOT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  UNIQUE INDEX `ff_id_UNIQUE` (`ff_id` ASC)  COMMENT '');
