SELECT * FROM Words 
WHERE dict_name == 'Наречия' 
AND _id >= 1 
AND count_repeat <> 0
LIMIT 10000000000