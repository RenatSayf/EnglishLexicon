INSERT OR IGNORE INTO Words 
(english, translate)
SELECT English, Translate 
FROM 'Наречия' LIMIT 3


