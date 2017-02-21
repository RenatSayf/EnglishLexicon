package com.myapp.lexicon;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// TODO:  IntentService class
public class z_speechService extends IntentService
{
    private static boolean stop = true;
    private ArrayList<p_ItemListDict> playList;
    private DatabaseHelper databaseHelper;
    private String textEn;
    private String textRu;
    private String textDict;

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

        if (databaseHelper == null)
        {
            databaseHelper = new DatabaseHelper(getApplicationContext());
            databaseHelper.create_db();
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
        try
        {
            databaseHelper.open();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stop = true;
        databaseHelper.close();
    }

    public static void stopIntentService()
    {
        stop = true;
    }

    public static void resetCount()
    {
//        dictIndex = 0; // Сброс индексов списков и элементов
//        wordIndex = 1;
    }

    private Intent updateIntent;
    @Override
    protected void onHandleIntent(Intent intent)
    {
        int order = intent.getIntExtra(getString(R.string.key_play_order), 0);
        stop = intent.getBooleanExtra(getString(R.string.is_one_time), true);
        updateIntent = new Intent();
        updateIntent.setAction(ACTION_UPDATE);
        updateIntent.addCategory(Intent.CATEGORY_DEFAULT);

        playList = a_MainActivity.getPlayList();
        if (playList.size() == 0) return;

        if (order == 0)
        {
            do
            {
                playList = a_MainActivity.getPlayList();
                if (playList.size() > 0)
                {
                    if (!AppData.get_isPause()) AppData.set_Ndict(0);
                    for (int i = AppData.get_Ndict(); i < playList.size(); i++)
                    {
                        p_ItemListDict playListItem = playList.get(i);
                        textDict = playListItem.get_dictName();
                        AppData.setCurrentDict(textDict);
                        AppData.set_Ndict(i);
                        int wordsCountInTable = getWordsCount(playListItem.get_dictName());
                        if (wordsCountInTable > 0)
                        {
                            if (!AppData.get_isPause()) AppData.set_Nword(1);
                            for (int j = AppData.get_Nword(); j <= wordsCountInTable; j++)
                            {
                                AppData.set_Nword(j);

//                                if (stop)
//                                {
//                                    a_SplashScreenActivity.speech.stop();
//                                    break;
//                                }
                                ArrayList<DataBaseEntry> list = getEntriesFromDB(playListItem.get_dictName(), j, j);
                                if (list.size() == 0)
                                {
                                    continue;
                                }

                                int repeat = 0;
                                try
                                {
                                    repeat = Integer.parseInt(list.get(0).get_count_repeat());
                                } catch (NumberFormatException e)
                                {
                                    repeat = 1;
                                }

                                if (a_MainActivity.settings.getBoolean(a_MainActivity.KEY_ENG_ONLY, true))
                                {
                                    for (int t = 0; t < repeat; t++)
                                    {
                                        try
                                        {
                                            speakWord(list.get(0), false);
                                        } catch (InterruptedException e)
                                        {
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                            break;
                                        }

//                                        if (stop)
//                                        {
//                                            a_SplashScreenActivity.speech.stop();
//                                            break;
//                                        }
                                    }
                                } else
                                {
                                    try
                                    {
                                        for (int t = 0; t < repeat; t++)
                                        {
                                            speakWord(list.get(0), true);
                                        }
                                    } catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }

//                                if (stop)
//                                {
//                                    a_SplashScreenActivity.speech.stop();
//                                    break;
//                                }
                                AppData.set_Nword(j);
                            }
                        } else
                        {
                            break;
                        }
//                        if (stop)
//                        {
//                            a_SplashScreenActivity.speech.stop();
//                            break;
//                        }
                        AppData.set_Ndict(i);
                        AppData.set_isPause(false);
                    }
                } else
                {
                    break;
                }
            } while (!stop);
        }

    }

    private void speakWord(final DataBaseEntry entries, boolean engOnly) throws InterruptedException
    {
        final boolean[] speek_done = {false};
        a_SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String utteranceId)
            {
                if (utteranceId.equals("ru"))
                {
                    textRu = entries.get_translate();
                }
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
                    return;
                }
                if (utteranceId.equals("en"))
                {
                    textRu = entries.get_translate();
                    HashMap<String,String> mapRu = new HashMap<>();
                    mapRu.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ru");
                    a_SplashScreenActivity.speech.setLanguage(Locale.getDefault());
                    a_SplashScreenActivity.speech.speak(textRu, TextToSpeech.QUEUE_ADD, mapRu);
                }
                if (utteranceId.equals("ru"))
                {
                    speek_done[0] = true;
                }
            }

            @Override
            public void onError(String utteranceId)
            {
                Toast.makeText(getApplicationContext(), R.string.speech_error, Toast.LENGTH_SHORT).show();
                speek_done[0] = true;
            }
        });

        HashMap<String,String> mapEn = new HashMap<>();
        if (!engOnly)
        {
            mapEn.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "en");
        }
        else
        {
            mapEn.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ru");
        }
        textEn = entries.get_english();
        textRu = "";
        a_SplashScreenActivity.speech.setLanguage(Locale.US);
        a_SplashScreenActivity.speech.speak(textEn, TextToSpeech.QUEUE_ADD, mapEn);

        while (!speek_done[0])
        {
            try
            {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        return;
    }

    private int getWordsCount(String dictName)
    {
        int count = 0;
        Cursor cursor = null;
        try
        {
            if (databaseHelper.database.isOpen())
            {

                cursor = databaseHelper.database.query(dictName, null, null, null, null, null, null);
                count = cursor.getCount();
            }
        }
        catch (Exception e)
        {
            count = 0;
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return count;
    }

    public ArrayList<DataBaseEntry> getEntriesFromDB(String tableName, int startId, int endId)
    {
        ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId, null);
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast())
                    {
                        DataBaseEntry dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1), cursor.getString(3));
                        entriesFromDB.add(dataBaseEntry);
                        cursor.moveToNext();
                    }
                }
            }
        }
        catch (Exception e)
        {
            entriesFromDB.add(new DataBaseEntry(null,null, null));
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return entriesFromDB;
    }


}
