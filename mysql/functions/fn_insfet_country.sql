DELIMITER $$
CREATE FUNCTION `fn_insfet_country` (lu_name VARCHAR(128))
RETURNS int(11)
BEGIN
	DECLARE ret_id INT default (select `id` from `country` where `country`.`name` = lu_name);
    if (ret_id is null) then
		insert into `country` (`name`) values (lu_name);
        return last_insert_id();
	end if;
	RETURN ret_id;
END$$
DELIMITER ;
