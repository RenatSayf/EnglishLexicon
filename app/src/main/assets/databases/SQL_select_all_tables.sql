SELECT name FROM sqlite_master WHERE type='table' 
AND name <> 'android_metadata' 
AND name <> 'com_myapp_lexicon_api_keys'
AND name <> 'sqlite_sequence'
AND name <> 'Words' 
ORDER BY name