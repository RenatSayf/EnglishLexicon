SELECT count() FROM Words WHERE _id <= 8 AND dict_name == 'Прилагательные' AND count_repeat > 0 
UNION ALL
SELECT count() FROM Words WHERE dict_name == 'Прилагательные'
UNION ALL
SELECT count() FROM Words WHERE dict_name == 'Прилагательные' AND count_repeat <= 0