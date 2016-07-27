package com.myapp.lexicon;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ренат on 14.04.2016.
 */
public class z_speechSynthesAsync extends AsyncTask<Context, String, Void>
{

    private final Context _context;
    private final TextView _textViewEn;
    private final TextView _textViewRu;
    private TextToSpeech _textToSpeech;
    private boolean _enIsSupport = true;
    private boolean _ruIsSupport = true;
    private String[] _listWord = new String[2];
    private boolean _cancelled;
    private ArrayList<p_ItemListDict> _playList;
    private DatabaseHelper _databaseHelper;
    private HashMap<String, String> _map = new HashMap<String, String>();

    public z_speechSynthesAsync(Context context, TextView textViewEn, TextView textViewRu)
    {
        this._context=context;
        this._textViewEn=textViewEn;
        this._textViewRu=textViewRu;
        _textToSpeech=new TextToSpeech(context, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                Log.i("Lexicon", "Вход в z_speechSynthesAsync.onInit()");
                // TODO Auto-generated method stub
                _map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "text");
                if (status == TextToSpeech.SUCCESS)
                {
                    int resultEn = _textToSpeech.setLanguage(Locale.UK);
                    int resultRu = _textToSpeech.setLanguage(Locale.getDefault());

                    if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Log.i("Lexicon", "Извините, английский язык не поддерживается");
                        _enIsSupport = false;
                    }
                    if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Log.i("Lexicon", "Извините, русский язык не поддерживается");
                        _ruIsSupport = false;
                    }

                } else
                {
                    Log.i("Lexicon", "z_speechSynthesAsync.onInit() - Ошибка!");
                }
                Log.i("Lexicon", "Выход из onInit()");
            }
        });
        if (_databaseHelper == null)
        {
            _databaseHelper = new DatabaseHelper(context);
            _databaseHelper.create_db();
        }

    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Context... params)
    {
        String[] data = new String[2];
        _playList = a_MainActivity.getPlayList();
        while (!isCancelled())
        {
            if (isCancelled())
            {
                Log.e("Lexicon", "z_speechSynthesAsync doInBackground() isCancelled = " + isCancelled());
                break;
            }
            Log.e("Lexicon", "z_speechSynthesAsync doInBackground() isCancelled = " + isCancelled());
            if (_playList.size() > 0)
            {
                if(!AppData.get_isPause()) AppData.set_Ndict(0);
                for (int i = AppData.get_Ndict(); i < _playList.size(); i++)
                {
                    if (isCancelled())
                    {
                        Log.e("Lexicon", "z_speechSynthesAsync doInBackground() AppData.get_Ndict(i) = " + AppData.get_Ndict());
                        break;
                    }
                    p_ItemListDict playListItem = _playList.get(i);
                    int wordsCountInTable = getWordsCount(playListItem.get_dictName());
                    if (wordsCountInTable > 0)
                    {
                        if(!AppData.get_isPause()) AppData.set_Nword(1);
                        for (int j = AppData.get_Nword(); j <= wordsCountInTable; j++)
                        {

                            if (isCancelled())
                            {
                                Log.e("Lexicon", "z_speechSynthesAsync doInBackground() AppData.get_Nword() = " + AppData.get_Nword());
                                break;
                            }
                            ArrayList<DataBaseEntry> list = getOneEntryFromTable(playListItem.get_dictName(), j);
                            Log.e("Lexicon", list.get(0).get_english() + "    " + list.get(0).get_translate());

                            try
                            {
                                //AppData.enText = speakWordEn(list);
                                data[0] = speakWordEn(list);
                                AppData.set_Nword(j);
                                AppData.set_Ndict(i);
                                data[1] = "";
                                publishProgress(data);
                                data[1] = speakWordRu(list);
                                publishProgress(data);
                                Log.e("Lexicon", "z_speechSynthesAsync.doInBackground() data[] = " + data[0] + "   " + data[1]);
                            } catch (InterruptedException e)
                            {
                                Log.i("Lexicon", "ИСКЛЮЧЕНИЕ в z_speechSynthesAsync.speakWord(list) - " + e);
                                e.printStackTrace();
                            }
                            AppData.set_isPause(false);
                        }
                    } else
                    {
                        Log.e("Lexicon", "z_speechSynthesAsync wordsCountInTable = " + wordsCountInTable);
                        break;
                    }
                    AppData.set_Ndict(i);
                }
            } else
            {
                Log.e("Lexicon", "z_speechSynthesAsync  _playList.size() = " + _playList.size());
                break;
            }
            AppData.set_isPause(false);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values)
    {
        Log.e("Lexicon", "VoiceSynthesizer.onProgressUpdate values = " + values[0].toString() + "   " + values[1].toString());
        //_textViewEn.setText(values[0].toString());
        //_textViewEn.setText(AppData.enText);
        //AppData.textViewEn.setText(values[0]);
        //AppData.textViewRu.setText(values[1]);
        //_textViewRu.setText(values[1].toString());
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);
        Log.e("Lexicon", "z_speechSynthesAsync.onPostExecute()  isCancelled() = " + isCancelled());
    }

    private int speakWord(String text) throws InterruptedException
    {
        Log.e("Lexicon", "Вход в z_speechSynthesAsync.speakWord()  text = " + text);
        int res = -1;
        _textToSpeech.setLanguage(Locale.UK);
            _textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener()
            {
                @Override
                public void onStart(String utteranceId)
                {

                }

                @Override
                public void onDone(String utteranceId)
                {

                }

                @Override
                public void onError(String utteranceId)
                {

                }
            });
            Log.e("Lexicon", "Вход в z_speechSynthesAsync.speakWord()  _textToSpeech.getLanguage() = " + _textToSpeech.getLanguage().getDisplayLanguage());
            res = _textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, _map);


        return res;
    }
    private String speakWordEn(ArrayList<DataBaseEntry> listWord) throws InterruptedException
    {
        String res = null;
        Log.e("Lexicon", "Вход в z_speechSynthesAsync.speakWordEn()  listWord.size = " + listWord.size());
        for (int i = 0; i < listWord.size(); i++)
        {
            int resultEn = _textToSpeech.setLanguage(Locale.UK);
            if (resultEn != TextToSpeech.LANG_MISSING_DATA && resultEn != TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("Lexicon", "Вход в z_speechSynthesAsync.speakWordEn()  _textToSpeech.getLanguage() = " + _textToSpeech.getLanguage().getDisplayLanguage());
                res = listWord.get(i).get_english();
                _textToSpeech.speak(listWord.get(i).get_english(), TextToSpeech.QUEUE_ADD, null);
                while (_textToSpeech.isSpeaking())
                {
                    TimeUnit.MILLISECONDS.sleep(1);
                }
            } else
            {
                Log.i("Lexicon", "speakWordEn() Извините, английский язык не поддерживается");
            }
        }
        return res;
    }
    private String speakWordRu(ArrayList<DataBaseEntry> listWord) throws InterruptedException
    {
        String res = null;
        Log.e("Lexicon", "Вход в z_speechSynthesAsync.speakWordRu()  listWord.size = " + listWord.size());
        for (int i = 0; i < listWord.size(); i++)
        {
            int resultRu = _textToSpeech.setLanguage(Locale.getDefault());
            if (resultRu != TextToSpeech.LANG_MISSING_DATA && resultRu != TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("Lexicon", "Вход в z_speechSynthesAsync.speakWordRu()  _textToSpeech.getLanguage() = " + _textToSpeech.getLanguage().getDisplayLanguage());
                res = listWord.get(i).get_translate();
                _textToSpeech.speak(listWord.get(i).get_translate(), TextToSpeech.QUEUE_ADD, null);
                while (_textToSpeech.isSpeaking())
                {
                    TimeUnit.MILLISECONDS.sleep(1);
                }
            } else
            {
                Log.i("Lexicon", "speakWordRu() Извините, русский язык не поддерживается");
            }

        }
        return res;
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
            Log.i("Lexicon", "z_speechSynthesAsync.getWordsCount() - " + count);
        }
        catch (Exception e)
        {
            Log.i("Lexicon", "ИСКЛЮЧЕНИЕ в z_speechSynthesAsync.getWordsCount() - " + e);
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
            Log.i("Lexicon", "ИСКЛЮЧЕНИЕ в z_speechSynthesAsync.getOneEntryFromTable() - " + e);
        } finally
        {
            _databaseHelper.close();
        }
        return entries;
    }
    public void setStop()
    {
        _textToSpeech.stop();
        _textToSpeech.shutdown();
        cancel(true);
        _cancelled = true;
        Log.e("Lexicon", "z_speechSynthesAsync.setStop() isCancelled() = " + isCancelled());

    }

    @Override
    protected void onCancelled()
    {
        _textToSpeech.stop();
        _textToSpeech.shutdown();
        super.onCancelled();
        Log.e("Lexicon", "z_speechSynthesAsync.onCancelled() isCancelled() = " + isCancelled());

    }
}
