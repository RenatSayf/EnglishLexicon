package com.myapp.lexicon;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ренат on 13.02.2017.
 */

public class z_speechService2 extends IntentService
{
    public static final String ACTION_UPDATE = "com.myapp.lexicon";
    public static final String EXTRA_KEY_UPDATE_EN = "EXTRA_UPDATE_EN";
    public static final String EXTRA_KEY_UPDATE_RU = "EXTRA_UPDATE_RU";

    private static boolean stop = false;
    private static int dictIndex = 0;
    private static int wordIndex = 1;
    public static TextToSpeech referens;

    private Intent updateIntent;
    private String textEn;
    private String textRu;
    private HashMap<String,String> hashMap = new HashMap<>();
    private TextToSpeech speech;
    private static z_RandomNumberGenerator wordIndexGen;
    private DatabaseHelper databaseHelper;
    private DataBaseQueries dataBaseQueries;
    private ArrayList<String> playList;

    private static IStartStopService iStartStopService;
    public static void setStartStopListeners(IStartStopService listeners)
    {
        iStartStopService = listeners;
    }

    public interface IStartStopService
    {
        void onStartSpeechService();
        void onStopSpeechService();
    }

    public z_speechService2()
    {
        super("com.myapp.lexicon");
    }

    public static void setSpeechReferens(TextToSpeech speech_ref)
    {
        referens = speech_ref;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        speech = referens;

        if (databaseHelper == null)
        {
            databaseHelper = new DatabaseHelper(getApplicationContext());
            databaseHelper.create_db();
        }
        try
        {
            dataBaseQueries = new DataBaseQueries(this);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        playList = getPlayList();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        stop = false;
        if (iStartStopService != null)
        {
            iStartStopService.onStartSpeechService();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {

        super.onDestroy();
    }
    public static void stopIntentService()
    {
        stop = true;
        if (iStartStopService != null)
        {
            iStartStopService.onStopSpeechService();
        }
    }

    public ArrayList<String> getPlayList()
    {
        ArrayList<String> listDicts=new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.key_play_list), MODE_PRIVATE);
        String play_list_items = sharedPreferences.getString(getString(R.string.play_list_items), null);
        if (play_list_items != null && play_list_items.length() > 0)
        {
            String[] splitArray = play_list_items.split(" ");
            for (int i = 0; i < splitArray.length; i++)
            {
                listDicts.add(i, splitArray[i]);
            }
        }

        return listDicts;
    }

    int wordsQuantity;
    @Override
    protected void onHandleIntent(Intent intent)
    {
        int play_order = intent.getIntExtra(getString(R.string.key_play_order), 0);
        updateIntent = new Intent();
        updateIntent.setAction(ACTION_UPDATE);
        updateIntent.addCategory(Intent.CATEGORY_DEFAULT);

        if (play_order == 1 && wordIndex == 0 && dictIndex == 0)
        {
            dictIndex = playList.size()-1;
            wordIndex = getWordsCount(playList.get(dictIndex));
        }

        if (play_order == 2)
        {
            for (int i = 0; i < playList.size(); i++)
            {
                wordsQuantity += playList.size();
            }
            Date date = new Date();
            wordIndexGen = new z_RandomNumberGenerator(wordsQuantity, (int) date.getTime());
            wordIndex = wordIndexGen.generate();
        }

        playWords(wordIndex, play_order);
        while (!stop) // пока stop = false, выполняется код в onHandleIntent
        {
            try
            {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void resetCount()
    {
        dictIndex = 0; // Сброс индексов списков и элементов
        wordIndex = 1;
    }

    int count_repeat = 0;
    int done_repeat = 0;
    private void playWords(int word_index, final int order)
    {
        switch (order)
        {
            case 0: // Прямое воспроизведение слов
                if (word_index >= getWordsCount(playList.get(dictIndex)))
                {
                    // Если индекс элемента текущего списка >= количества эл. в этом списке, счетчик эл. обнуляем, индекс списка увелич на 1
                    wordIndex = 1;
                    dictIndex++;
                }
                if (dictIndex >= playList.size())
                {
                    // Если индекс списка >= общего колич. списков - индекс списка обнуляем
                    wordIndex = 1;
                    dictIndex = 0;
                }
                break;
            case 1: // Обратное воспроизведение слов
                if (wordIndex < 1 && dictIndex < 0)
                {
                    dictIndex = playList.size() - 1;
                    wordIndex = getWordsCount(playList.get(dictIndex));
                }
                if (word_index < 0)
                {
                    dictIndex--;
                    if (dictIndex >= 0)
                    {
                        wordIndex = getWordsCount(playList.get(dictIndex));
                    }
                }
                if (dictIndex < 0)
                {
                    dictIndex = playList.size()-1;
                    wordIndex = getWordsCount(playList.get(dictIndex));
                }
                break;
            case 2: // Случайное воспроизведение слов
                Date date = new Date();
                if (wordIndex < 1)
                {
                    wordIndexGen = new z_RandomNumberGenerator(wordsQuantity, (int) date.getTime());
                    wordIndex = wordIndexGen.generate();
                }

                int i = 1;
                while (i <= playList.size())
                {
                    if (wordIndex >= playList.size())
                    {
                        wordIndex = wordIndex - getWordsCount(playList.get(i));
                        i++;
                    }
                    else
                    {
                        dictIndex = i;
                        break;
                    }
                }
                break;
            default:break;
        }

        final int tempListIndex = dictIndex;
        final int tempWordIndex = wordIndex;
        final ArrayList<DataBaseEntry> list = dataBaseQueries.getEntriesFromDB(playList.get(dictIndex), wordIndex, wordIndex);
        //done_repeat = 0;
//        if (done_repeat == 0)
//        {
//            done_repeat = Integer.parseInt(list.get(0).get_count_repeat());
//        }

        speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String utteranceId)
            {
                if (stop) return;
                updateIntent.putExtra(EXTRA_KEY_UPDATE_EN, textEn);
                updateIntent.putExtra(EXTRA_KEY_UPDATE_RU, textRu);
                sendBroadcast(updateIntent);
            }

            @Override
            public void onDone(String utteranceId)
            {

                if (utteranceId.equals("en"))
                {
                    String translate = list.get(0).get_translate();
                    speakWord(translate);
                }
                if (utteranceId.equals("ru"))
                {
                    if (stop) return;
                    //done_repeat++;
                    count_repeat = Integer.parseInt(list.get(0).get_count_repeat());
                    if (done_repeat > count_repeat)
                    {
                        done_repeat = 0;
                        switch (order)
                        {
                            case 0:

                                wordIndex++;
                                break;
                            case 1:
                                wordIndex--;
                                break;
                            case 2:
                                wordIndex = wordIndexGen.generate();
                                break;
                            default:break;
                        }

                        playWords(wordIndex, order);
                    }

                    if (done_repeat <= count_repeat)
                    {
                        done_repeat++;
                        playWords(wordIndex, order);
                    }
                }
                return;
            }

            @Override
            public void onError(String utteranceId)
            {

            }
        });

        if (done_repeat < Integer.parseInt(list.get(0).get_count_repeat()))
        {
            String english = list.get(0).get_english();
            speakWord(english);

        }
        else
        {
            switch (order)
            {
                case 0:
                    wordIndex++;
                    break;
                case 1:
                    wordIndex--;
                    break;
                case 2:
                    wordIndex = wordIndexGen.generate();
                    break;
                default:break;
            }
            done_repeat = 0;
            playWords(wordIndex, order);

        }
        //playWords(wordIndex, order);
    }

    private void speakWord(String text)
    {
        if (stop) return;
        String[] langOfText = getLangOfText(text);
        if (langOfText[1].equals("en"))
        {
            textEn = text;
            textRu = "";
            speech.setLanguage(Locale.US);
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "en");
        }
        else if (langOfText[1].equals("ru"))
        {
            textRu = text;
            speech.setLanguage(Locale.getDefault());
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ru");
        }
        else if (langOfText[1] == null)
        {
            Toast.makeText(this,getString(R.string.text_msg_not_match_lang) + text, Toast.LENGTH_SHORT).show();
            return;
        }
        speech.speak(text, TextToSpeech.QUEUE_ADD, hashMap);
    }

    private int getWordsCount(String dictName)
    {
        int count;
        try
        {
            databaseHelper.open();
            SQLiteDatabase database1 = databaseHelper.database;
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
            databaseHelper.close();
        }
        return count;
    }

    public String[] getLangOfText(String text)
    {
        String str = text;
        String[] lang = new String[2];
        int char_first;
        for (int i = 0; i < str.length(); i++)
        {
            char_first = str.codePointAt(i);
            if ((char_first >= 33 && char_first <= 64) || (str.codePointAt(i) >= 91 && str.codePointAt(i) <= 96) || (str.codePointAt(i) >= 123 && str.codePointAt(i) <= 126))
            {
                continue;
            }
            if (str.codePointAt(i) >= 1025 && str.codePointAt(i) <= 1105)
            {
                lang[0] = "ru-en";
                lang[1] = "ru";
            }
            else if (str.codePointAt(i) >= 65 && str.codePointAt(i) <= 122)
            {
                lang[0] = "en-ru";
                lang[1] = "en";
            }
            else
            {
                lang[0] = null;
                lang[1] = null;
            }
        }
        return lang;
    }
}
