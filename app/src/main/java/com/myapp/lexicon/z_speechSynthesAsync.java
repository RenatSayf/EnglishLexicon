package com.myapp.lexicon;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ренат on 14.04.2016.
 */
public class z_speechSynthesAsync extends AsyncTask<String, Void, Void>
{
    private static z_speechSynthesAsync ourInstance = new z_speechSynthesAsync();
    private TextToSpeech textToSpeech;
    private HashMap<String, String> map = new HashMap<String, String>();
    private z_StringOperations stringOperations;
    private static Context _context;

    public static z_speechSynthesAsync getInstance(Context context)
    {
        _context = context;
        return ourInstance;
    }

    private z_speechSynthesAsync()
    {}

    public void init(Context context)
    {
        textToSpeech =new TextToSpeech(context, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "text");
                if (status == TextToSpeech.SUCCESS)
                {
                    int resultEn = textToSpeech.isLanguageAvailable(Locale.UK);
                    if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Log.i("Lexicon", "Извините, английский язык не поддерживается");

                    }
                    int resultRu = textToSpeech.isLanguageAvailable(Locale.getDefault());
                    if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Log.i("Lexicon", "Извините, русский язык не поддерживается");

                    }

                } else
                {
                    Log.i("Lexicon", "z_speechSynthesAsync.onInit() - Ошибка!");
                }
                Log.i("Lexicon", "Выход из onInit()");
            }
        });
        stringOperations = z_StringOperations.getInstance();
    }

    @Override
    protected Void doInBackground(String... params)
    {
        String[] lang = stringOperations.getLangOfText(params[0]);
        if (lang[1] == "en")
        {
            textToSpeech.setLanguage(Locale.US);
        }
        if (lang[1] == "ru")
        {
            textToSpeech.setLanguage(Locale.getDefault());
        }
        textToSpeech.speak(params[0],TextToSpeech.QUEUE_ADD, map);
        return null;
    }

    @Override
    protected void onPreExecute()
    {
        //super.onPreExecute();
        init(_context);
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

    }


}
