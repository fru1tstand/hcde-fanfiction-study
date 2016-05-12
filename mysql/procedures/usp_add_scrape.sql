USE `fanfiction`;
DROP procedure IF EXISTS `usp_add_scrape`;

DELIMITER $$
USE `fanfiction`$$
CREATE PROCEDURE `usp_add_scrape` (
	session_name VARCHAR(128),
    scrape_date INT(10),
    url VARCHAR(255),
    content MEDIUMTEXT
)
BEGIN
	DECLARE session_id INT DEFAULT (SELECT fn_insfet_session(session_name));
    INSERT INTO `scrape` (`session_id`, `date`, `url`, `content`) VALUES (session_id, scrape_date, url, content);
END
$$

DELIMITER ;

