SELECT count() FROM Words WHERE dict_name = 'Наречия' AND count_repeat == 0
UNION
SELECT _id FROM Words WHERE dict_name = 'Наречия'



