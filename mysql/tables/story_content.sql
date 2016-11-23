CREATE TABLE `story_content_batch` (
	`id` int(11) NOT NULL AUTO_INCREMENT,
	`ff_story_id` int(11) NOT NULL,
    `chapter` int(11) NOT NULL,
    `content` LONGTEXT,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ff_story_id_chapter_UNIQUE` (`ff_story_id`, `chapter`),
    INDEX `ff_story_id_idx` (`ff_story_id` ASC)
 );
 