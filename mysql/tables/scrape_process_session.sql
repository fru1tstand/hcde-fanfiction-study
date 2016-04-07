CREATE TABLE `scrape_process_log` (
  `id` int(11) NOT NULL,
  `datetime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `scrape_process_log_session_id` int(11) NOT NULL,
  `scrape_process_log_status_id` int(11) NOT NULL,
  `scrape_raw_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_scrape_process_log_scrape_process_log_session_idx` (`scrape_process_log_session_id`),
  KEY `fk_scrape_process_log_scrape_process_log_status_idx` (`scrape_process_log_status_id`),
  KEY `fk_scrape_process_log_scrape_raw_idx` (`scrape_raw_id`),
  CONSTRAINT `fk_scrape_process_log_scrape_process_log_session` FOREIGN KEY (`scrape_process_log_session_id`) REFERENCES `scrape_process_log_session` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_scrape_process_log_scrape_process_log_status` FOREIGN KEY (`scrape_process_log_status_id`) REFERENCES `scrape_process_log_status` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_scrape_process_log_scrape_raw` FOREIGN KEY (`scrape_raw_id`) REFERENCES `scrape_raw` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8