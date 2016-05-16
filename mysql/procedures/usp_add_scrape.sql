USE `fanfiction`;
DROP procedure IF EXISTS `usp_add_scrape`;

DELIMITER $$
USE `fanfiction`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `usp_add_scrape`(
	in_session_name VARCHAR(128),
    in_scrape_date INT(10),
    in_url VARCHAR(255),
    in_content MEDIUMTEXT
)
BEGIN
	DECLARE v_session_id INT DEFAULT (SELECT fn_insfet_session(in_session_name));
    INSERT INTO `scrape` (`session_id`, `date`, `url`, `content`) VALUES (v_session_id, in_scrape_date, in_url, in_content);
END$$

DELIMITER ;

