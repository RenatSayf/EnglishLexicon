package com.myapp.lexicon;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

// TODO:  IntentService class
public class z_speechService extends IntentService
{
    private boolean stop = true;
    private ArrayList<p_ItemListDict> _playList;
    private DatabaseHelper _databaseHelper;
    private DataBaseQueries dataBaseQueries;

    public static final String ACTION_UPDATE = "com.myapp.lexicon.UPDATE";
    public static final String EXTRA_KEY_UPDATE_EN = "EXTRA_UPDATE_EN";
    public static final String EXTRA_KEY_UPDATE_RU = "EXTRA_UPDATE_RU";
    public static final String EXTRA_KEY_UPDATE_DICT = "EXTRA_UPDATE_DICT";

    public z_speechService()
    {
        super("LexiconSpeechService");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        if (_databaseHelper == null)
        {
            _databaseHelper = new DatabaseHelper(getApplicationContext());
            _databaseHelper.create_db();
        }
        try
        {
            dataBaseQueries = new DataBaseQueries(this);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        stop = false;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stop = true;
        z_Log.v("Разрушаем процесс");
    }

    private Intent updateIntent;
    @Override
    protected void onHandleIntent(Intent intent)
    {
        updateIntent = new Intent();
        updateIntent.setAction(ACTION_UPDATE);
        updateIntent.addCategory(Intent.CATEGORY_DEFAULT);

        _playList = a_MainActivity.getPlayList();
        if (_playList.size() == 0) return;

        while (!stop)
        {
            _playList = a_MainActivity.getPlayList();
            if (_playList.size() > 0)
            {
                if(!AppData.get_isPause()) AppData.set_Ndict(0);
                for (int i = AppData.get_Ndict(); i < _playList.size(); i++)
                {
                    p_ItemListDict playListItem = _playList.get(i);
                    textDict = playListItem.get_dictName();
                    AppData.setCurrentDict(textDict);
                    AppData.set_Ndict(i);
                    int wordsCountInTable = getWordsCount(playListItem.get_dictName());
                    if (wordsCountInTable > 0)
                    {
                        if(!AppData.get_isPause()) AppData.set_Nword(1);
                        for (int j = AppData.get_Nword(); j <= wordsCountInTable; j++)
                        {
                            AppData.set_Nword(j);

                            if (stop)
                            {
                                z_Log.v(" onHandleIntent() stop = " + stop);
                                a_SplashScreenActivity.speech.stop();
                                break;
                            }
                            ArrayList<DataBaseEntry> list = dataBaseQueries.getEntriesFromDB(playListItem.get_dictName(), j, j);
                            if (list.size() == 0)
                            {
                                continue;
                            }
                            int repeat = Integer.parseInt(list.get(0).get_count_repeat());

                            if (a_MainActivity.settings.getBoolean(a_MainActivity.KEY_ENG_ONLY,true))
                            {
                                for (int t = 0; t < repeat; t++)
                                {
                                    try
                                    {
                                        speakWord(list.get(0).get_english(), Locale.US);
                                    } catch (InterruptedException e)
                                    {
                                        z_Log.v("Исключение - "+e.getMessage());
                                        e.printStackTrace();
                                    }

                                    if (stop)
                                    {
                                        a_SplashScreenActivity.speech.stop();
                                        break;
                                    }

                                    try
                                    {
                                        speakWord(list.get(0).get_translate(), Locale.getDefault());
                                    } catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            else
                            {
                                try
                                {
                                    for (int t = 0; t < repeat; t++)
                                    {
                                        speakEnglishOnly(list.get(0).get_english(),list.get(0).get_translate());
                                    }
                                } catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            }

                            if (stop)
                            {
                                a_SplashScreenActivity.speech.stop();
                                break;
                            }
                            AppData.set_Nword(j);
                        }
                    } else
                    {
                        break;
                    }
                    if (stop)
                    {
                        a_SplashScreenActivity.speech.stop();
                        break;
                    }
                    AppData.set_Ndict(i);
                    AppData.set_isPause(false);
                }
            } else
            {
                break;
            }
        }

    }

    private String textEn;
    private String textRu;
    private String textDict;

    private String speakWord(String text, Locale lang) throws InterruptedException
    {
        if (lang == Locale.US)
        {
            textEn = text;
            textRu = "";
        }
        else if (lang != Locale.US)
        {
            textRu = text;
        }
        else
        {
            z_Log.v("speakWord() Текст не соответствует языку = " + text);
            return null;
        }
        a_SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String utteranceId)
            {
//                z_Log.v("  Начинаем синтез речи utteranceId = " + utteranceId);
//                z_Log.v("  _wordses  textEn = " + textEn + "    " + "textRu = " + textRu);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_EN, textEn);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_RU, textRu);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_DICT, textDict);
                sendBroadcast(updateIntent);
            }

            @Override
            public void onDone(String utteranceId)
            {
                if (stop)
                {
                    a_SplashScreenActivity.speech.stop();
                }

            }

            @Override
            public void onError(String utteranceId)
            {
                //z_Log.v("Ошибка синтеза");
                updateIntent.putExtra(EXTRA_KEY_UPDATE_RU, textRu);
                sendBroadcast(updateIntent);
            }
        });

        Locale language = a_SplashScreenActivity.speech.getLanguage();
        if (language != lang)
        {
            //a_SplashScreenActivity.speech.stop();
            a_SplashScreenActivity.speech.setLanguage(lang);
        }
        int count = 0;
        int res = -3;
        while (res < 0)
        {
            Thread.sleep(100);
            res = a_SplashScreenActivity.speech.isLanguageAvailable(lang);
            count++;
            if (count >= 2000)
            {
                z_Log.v("textToSpeech.isLanguageAvailable(lang) = " + res + ".    " + lang.getDisplayName() + " язык недоступен");
                //text = null;
                break;
            }
        }
        if (text == null || res <0)
        {
            return text;
        }
        if (stop)
        {
            a_SplashScreenActivity.speech.stop();
            return text;
        }
        z_Log.v("textToSpeech.setLanguage(lang) = " + res + "    count = " + count);
        a_SplashScreenActivity.speech.speak(text, TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);

        while (a_SplashScreenActivity.speech.isSpeaking())
        {
            if (stop)
            {
                a_SplashScreenActivity.speech.stop();
                break;
            }
            Thread.sleep(100);
        }
        z_Log.v("speakWord()   text = " + text);

        return text;
    }
    private String speakEnglishOnly(String text_en, String text_ru) throws InterruptedException
    {
        Locale lang = Locale.US;
        textEn = text_en;
        textRu = text_ru;
        a_SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String utteranceId)
            {
                z_Log.v("  Начинаем синтез речи utteranceId = " + utteranceId);
                z_Log.v("  _wordses  textEn = " + textEn + "    " + "textRu = " + textRu);
            }

            @Override
            public void onDone(String utteranceId)
            {
                updateIntent.putExtra(EXTRA_KEY_UPDATE_EN, textEn);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_RU, textRu);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_DICT, textDict);
                sendBroadcast(updateIntent);
                if (stop)
                {
                    a_SplashScreenActivity.speech.stop();
                }
            }

            @Override
            public void onError(String utteranceId)
            {
                z_Log.v("Ошибка синтеза");
                updateIntent.putExtra(EXTRA_KEY_UPDATE_RU, textRu);
                sendBroadcast(updateIntent);
            }
        });

        Locale language = a_SplashScreenActivity.speech.getLanguage();
        if (language != lang)
        {
            a_SplashScreenActivity.speech.stop();
            a_SplashScreenActivity.speech.setLanguage(lang);
        }
        a_SplashScreenActivity.speech.playSilence(1500, TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);
        int res = -3;
        int count = 0;
        while (res < 0)
        {
            Thread.sleep(10);
            res = a_SplashScreenActivity.speech.isLanguageAvailable(lang);
            count++;
            if (count >= 2000)
            {
                z_Log.v("textToSpeech.isLanguageAvailable(lang) = " + res + ".    " + lang.getDisplayName() + " язык недоступен");
                break;
            }
        }
        if (text_en == null || res <0)
        {
            return null;
        }
        z_Log.v("textToSpeech.setLanguage(lang) = " + res + "    count = " + count);
        a_SplashScreenActivity.speech.speak(text_en, TextToSpeech.QUEUE_ADD, a_SplashScreenActivity.map);

        while (a_SplashScreenActivity.speech.isSpeaking())
        {
            if (stop)
            {
                a_SplashScreenActivity.speech.stop();
                break;
            }
            Thread.sleep(10);
        }
        z_Log.v("speakWord()   text = " + text_en);
        return text_en;
    }

    private int getWordsCount(String dictName)
    {
        int count;
        try
        {
            _databaseHelper.open();
            SQLiteDatabase database1 = _databaseHelper.database;
            Cursor cursor=database1.query(dictName, null, null, null, null, null, null);
            count = cursor.getCount();
            Log.i("Lexicon", "z_Speaker.getWordsCount() - " + count);
        }
        catch (Exception e)
        {
            Log.i("Lexicon", "ИСКЛЮЧЕНИЕ в z_Speaker.getWordsCount() - " + e);
            count = 0;
        }finally
        {
            _databaseHelper.close();
        }
        return count;
    }


}
