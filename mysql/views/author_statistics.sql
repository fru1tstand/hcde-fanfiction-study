CREATE
VIEW `author_statistics` AS
    SELECT 
        `scrape_book_result_element`.`ff_author_id` AS `Author ID`,
        `scrape_book_result_element`.`author` AS `Author Name`,
        COUNT(DISTINCT `scrape_book_result_element`.`ff_book_id`) AS `Stories`,
        COUNT(DISTINCT `scrape_book_result_ff_genre`.`ff_genre_id`) AS `Genres Written For`,
        COUNT(DISTINCT `scrape_book_result_ff_character`.`ff_character_id`) AS `Characters used`,
        FROM_UNIXTIME(MIN(`scrape_book_result_element`.`meta_date_published`)) AS `First Seen`,
        FROM_UNIXTIME(MAX(`scrape_book_result_element`.`meta_date_published`)) AS `Last Seen`,
        MAX(`scrape_book_result_element`.`meta_words`) AS `Most Words`,
        MIN(`scrape_book_result_element`.`meta_words`) AS `Least Words`,
        AVG(`scrape_book_result_element`.`meta_words`) AS `Average # of Words`,
        MAX(`scrape_book_result_element`.`meta_reviews`) AS `Most Reviews`,
        MIN(`scrape_book_result_element`.`meta_reviews`) AS `Least Reviews`,
        AVG(`scrape_book_result_element`.`meta_reviews`) AS `Average # of Reviews`,
        MAX(`scrape_book_result_element`.`meta_favorites`) AS `Most Favorites`,
        MIN(`scrape_book_result_element`.`meta_favorites`) AS `Least Favorites`,
        AVG(`scrape_book_result_element`.`meta_favorites`) AS `Average # of Favorites`,
        MAX(`scrape_book_result_element`.`meta_followers`) AS `Most Follows`,
        MIN(`scrape_book_result_element`.`meta_followers`) AS `Least Follows`,
        AVG(`scrape_book_result_element`.`meta_followers`) AS `Average # of Follows`,
        MAX(`scrape_book_result_element`.`meta_chapters`) AS `Most Chapters`,
        MIN(`scrape_book_result_element`.`meta_chapters`) AS `Least Chapters`,
        AVG(`scrape_book_result_element`.`meta_chapters`) AS `Average # of Chapters`
    FROM
        ((`scrape_book_result_element`
        JOIN `scrape_book_result_ff_genre` ON ((`scrape_book_result_ff_genre`.`ff_book_id` = `scrape_book_result_element`.`ff_book_id`)))
        JOIN `scrape_book_result_ff_character` ON ((`scrape_book_result_ff_character`.`ff_book_id` = `scrape_book_result_element`.`ff_book_id`)))
    GROUP BY `scrape_book_result_element`.`ff_author_id` , `scrape_book_result_element`.`author`