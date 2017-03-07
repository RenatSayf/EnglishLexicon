package com.myapp.lexicon;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// TODO:  IntentService class
public class z_speechService extends IntentService
{
    private static boolean stop = true;
    private ArrayList<String> playList;
    private DatabaseHelper databaseHelper;
    private AppSettings appSettings;
    private String textEn;
    private String textRu;
    private String textDict;
    private int countRepeat;
    private static boolean isEngOnly = false;

    public static final String ACTION_UPDATE = "com.myapp.lexicon.UPDATE";
    public static final String EXTRA_KEY_EN = "EXTRA_UPDATE_EN";
    public static final String EXTRA_KEY_RU = "EXTRA_UPDATE_RU";
    public static final String EXTRA_KEY_DICT = "EXTRA_UPDATE_DICT";
    public static final String EXTRA_KEY_COUNT_REPEAT = "extra_key_count_repeat";

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
        appSettings = new AppSettings(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        isEngOnly = appSettings.isEnglishSpeechOnly();
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
        try
        {
            databaseHelper.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        if (isReset)
        {
            AppData.set_Ndict(0);
            AppData.set_Nword(1);
        }
        else
        {
            AppData.setPause(true);
        }
    }

    public static void stopIntentService()
    {
        stop = true;
    }

    private static boolean isReset = false;

    public static void resetCounter(boolean param)
    {
        isReset = param;
    }

    public static void setEnglishOnly(boolean param)
    {
        isEngOnly = param;
    }

    @Override
    public boolean stopService(Intent name)
    {
        return super.stopService(name);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        return super.onUnbind(intent);
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

        playList = appSettings.getPlayList();

        if (playList.size() == 0) return;

        if (order == 0)
        {
            while (!stop)
            {
                playList = appSettings.getPlayList();
                if (playList.size() > 0)
                {
                    if (!AppData.isPause()) AppData.set_Ndict(0);
                    for (int i = AppData.get_Ndict(); i < playList.size(); i++)
                    {
                        String playListItem = playList.get(i);
                        textDict = playListItem;
                        //AppData.setCurrentDict(textDict);
                        AppData.set_Ndict(i);
                        int wordsCountInTable = getWordsCount(playListItem);
                        if (wordsCountInTable > 0)
                        {
                            if (!AppData.isPause())
                            {
                                AppData.set_Nword(1);
                            }
                            for (int j = AppData.get_Nword(); j <= wordsCountInTable; j++)
                            {
                                AppData.set_Nword(j);
                                ArrayList<DataBaseEntry> list = getEntriesFromDB(playListItem, j, j);
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
                                countRepeat = repeat;

                                if (isEngOnly)
                                {
                                    for (int t = 0; t < repeat; t++)
                                    {
                                        try
                                        {
                                            speakWord(list.get(0), true);
                                        } catch (InterruptedException e)
                                        {
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                            break;
                                        }
                                    }
                                } else
                                {
                                    try
                                    {
                                        for (int t = 0; t < repeat; t++)
                                        {
                                            speakWord(list.get(0), false);
                                        }
                                    } catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                AppData.set_Nword(j);
                            }
                        } else
                        {
                            break;
                        }
                        AppData.set_Ndict(i);
                        AppData.setPause(false);
                    }
                } else
                {
                    break;
                }
            }
        }

        if (order == 1)
        {
            while (!stop)
            {
                playList = appSettings.getPlayList();
                if (playList.size() > 0)
                {
                    if (!AppData.isPause()) AppData.set_Ndict(0);
                    for (int i = AppData.get_Ndict(); i < playList.size(); i++)
                    {
                        String playListItem = playList.get(i);
                        textDict = playListItem;
                        //AppData.setCurrentDict(playListItem);
                        AppData.set_Ndict(i);
                        int wordsCountInTable = getWordsCount(playListItem);
                        if (wordsCountInTable > 0)
                        {
                            if (!AppData.isPause())
                            {
                                AppData.set_Nword(wordsCountInTable);
                            }
                            for (int j = AppData.get_Nword(); j >= 1; j--)
                            {
                                AppData.set_Nword(j);
                                ArrayList<DataBaseEntry> list = getEntriesFromDB(playListItem, j, j);
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
                                countRepeat = repeat;

                                if (isEngOnly)
                                {
                                    for (int t = 0; t < repeat; t++)
                                    {
                                        try
                                        {
                                            speakWord(list.get(0), true);
                                        } catch (InterruptedException e)
                                        {
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                            break;
                                        }
                                    }
                                } else
                                {
                                    try
                                    {
                                        for (int t = 0; t < repeat; t++)
                                        {
                                            speakWord(list.get(0), false);
                                        }
                                    } catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                AppData.set_Nword(j);
                            }
                        } else
                        {
                            break;
                        }
                        AppData.set_Ndict(i);
                        AppData.setPause(false);
                    }
                } else
                {
                    break;
                }
            }
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
                updateIntent.putExtra(EXTRA_KEY_EN, textEn);
                updateIntent.putExtra(EXTRA_KEY_RU, textRu);
                updateIntent.putExtra(EXTRA_KEY_DICT, textDict);
                updateIntent.putExtra(EXTRA_KEY_COUNT_REPEAT, countRepeat);
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
        if (engOnly)
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
