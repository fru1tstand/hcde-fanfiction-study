CREATE 
VIEW `genre_statistics` AS
    SELECT 
        `ff_genre`.`name` AS `genre`,
        COUNT(DISTINCT `scrape_book_result_element`.`ff_book_id`) AS `Number of Stories`,
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
        `scrape_book_result_element`
        JOIN `scrape_book_result_ff_genre` ON `scrape_book_result_ff_genre`.`ff_book_id` = `scrape_book_result_element`.`ff_book_id`
        JOIN `ff_genre` ON `ff_genre`.`id` = `scrape_book_result_ff_genre`.`ff_genre_id`
    GROUP BY `ff_genre`.`name`