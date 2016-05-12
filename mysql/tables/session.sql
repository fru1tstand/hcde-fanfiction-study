CREATE TABLE `fanfiction`.`session` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '',
  `name` VARCHAR(128) NOT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  UNIQUE INDEX `session_name_UNIQUE` (`session_name` ASC)  COMMENT '');