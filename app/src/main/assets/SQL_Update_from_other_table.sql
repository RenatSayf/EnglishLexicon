INSERT OR IGNORE INTO Words 
(english, translate)
SELECT English, Translate 
FROM lexicon_DB_old.Наречия WHERE ROWID >= 4 LIMIT 14


