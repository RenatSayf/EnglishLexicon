package com.myapp.lexicon;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.Process;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class z_speechService extends IntentService
{
    public TextToSpeech textToSpeech;
    private HashMap<String, String> map = new HashMap<String, String>();
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

    public z_speechService(TextToSpeech textToSpeech)
    {
        super("LexiconSpeechService");
        //this.textToSpeech = textToSpeech;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "text");
                if (status == TextToSpeech.SUCCESS)
                {
                    int resultRu = textToSpeech.setLanguage(Locale.getDefault());
                    if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        z_Log.v("Конструктор.  Извините, русский язык не поддерживается");
                    }
                    int resultEn = textToSpeech.setLanguage(Locale.US);
                    if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        z_Log.v("Конструктор.  Извините, английский язык не поддерживается");
                    }
                }else
                {
                    z_Log.v("Конструктор.  status = " + status);
                }
                z_Log.v("Конструктор.  Выход из onInit()  status = " + status);

            }
        });
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
        textToSpeech.stop();
        textToSpeech.shutdown();
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
        while (!stop)
        {
            z_Log.v(" stop = " + stop);
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
                                textToSpeech.stop();
                                textToSpeech.shutdown();
                                break;
                            }
                            ArrayList<DataBaseEntry> list = dataBaseQueries.getEntriesFromDB(playListItem.get_dictName(), j, j);
                            if (list.size() == 0)
                            {
                                continue;
                            }
                            z_Log.v(list.get(0).get_english() + "    " + list.get(0).get_translate());

                            if (a_MainActivity.settings.getBoolean(a_MainActivity.KEY_ENG_ONLY,true))
                            {
                                try
                                {
                                    speakWord(list.get(0).get_english(), Locale.US);
                                } catch (InterruptedException e)
                                {
                                    z_Log.v("Исключение - "+e.getMessage());
                                    e.printStackTrace();
                                }

                                try
                                {
                                    speakWord(list.get(0).get_translate(), Locale.getDefault());
                                } catch (InterruptedException e)
                                {
                                    z_Log.v("Исключение - "+e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                try
                                {
                                    speakEnglishOnly(list.get(0).get_english(),list.get(0).get_translate());
                                } catch (InterruptedException e)
                                {
                                    z_Log.v("Исключение - "+e.getMessage());
                                    e.printStackTrace();
                                }
                            }

                            if (stop)
                            {
                                z_Log.v(" onHandleIntent() stop = " + stop);
                                textToSpeech.stop();
                                textToSpeech.shutdown();
                                break;
                            }
                            AppData.set_Nword(j);
                            //AppData.set_isPause(false);
                        }
                    } else
                    {
                        z_Log.v(" wordsCountInTable = " + wordsCountInTable);
                        break;
                    }
                    if (stop)
                    {
                        z_Log.v(" onHandleIntent() stop = " + stop);
                        textToSpeech.stop();
                        textToSpeech.shutdown();
                        break;
                    }
                    AppData.set_Ndict(i);
                    AppData.set_isPause(false);
                }
            } else
            {
                z_Log.v("  _playList.size() = " + _playList.size());
                break;
            }
            //AppData.set_isPause(false);
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
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String utteranceId)
            {
                z_Log.v("  Начинаем синтез речи utteranceId = " + utteranceId);
                z_Log.v("  _wordses  textEn = " + textEn + "    " + "textRu = " + textRu);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_EN, textEn);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_RU, textRu);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_DICT, textDict);
                sendBroadcast(updateIntent);
            }

            @Override
            public void onDone(String utteranceId){}

            @Override
            public void onError(String utteranceId)
            {
                z_Log.v("Ошибка синтеза");
                updateIntent.putExtra(EXTRA_KEY_UPDATE_RU, textRu);
                sendBroadcast(updateIntent);
            }
        });

        int res = -3;
        textToSpeech.setLanguage(lang);
        int count = 0;
        while (res < 0)
        {
            //TimeUnit.MILLISECONDS.sleep(1);
            Thread.sleep(10);
            res = textToSpeech.isLanguageAvailable(lang);
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
        z_Log.v("textToSpeech.setLanguage(lang) = " + res + "    count = " + count);
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, map);

        while (textToSpeech.isSpeaking())
        {
            //TimeUnit.MILLISECONDS.sleep(1);
            Thread.sleep(10);
        }
        z_Log.v("speakWord()   text = " + text);

        return text;
    }
    private String speakEnglishOnly(String text_en, String text_ru) throws InterruptedException
    {
        Locale lang = Locale.US;
        textEn = text_en;
        textRu = text_ru;
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener()
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
            }

            @Override
            public void onError(String utteranceId)
            {
                z_Log.v("Ошибка синтеза");
                updateIntent.putExtra(EXTRA_KEY_UPDATE_RU, textRu);
                sendBroadcast(updateIntent);
            }
        });

        int res = -3;
        textToSpeech.setLanguage(lang);
        textToSpeech.playSilence(1500, TextToSpeech.QUEUE_ADD, map);
        int count = 0;
        while (res < 0)
        {
            Thread.sleep(10);
            res = textToSpeech.isLanguageAvailable(lang);
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
        textToSpeech.speak(text_en, TextToSpeech.QUEUE_ADD, map);

        while (textToSpeech.isSpeaking())
        {
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

    private ArrayList<DataBaseEntry> getOneEntryFromTable(String dictName, int j)
    {
        ArrayList<DataBaseEntry> entries = null;
        try
        {
            _databaseHelper.open();
            DataBaseQueries dataBaseQueries = new DataBaseQueries(_databaseHelper.database);
            entries = dataBaseQueries.getEntriesFromDB(dictName, j, j);
        } catch (SQLException e)
        {
            Log.i("Lexicon", "ИСКЛЮЧЕНИЕ в z_Speaker.getOneEntryFromTable() - " + e);
        } finally
        {
            _databaseHelper.close();
        }
        return entries;
    }
}
