package com.myapp.lexicon;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Ренат on 14.04.2016.
 */
public abstract class z_Speaker implements TextToSpeech.OnInitListener
{
    private TextToSpeech speech;
    private HashMap<String, String> map = new HashMap<String, String>();

    public z_Speaker(Context context)
    {
        speech = new TextToSpeech(context, this);
    }
    @Override
    public void onInit(int i)
    {
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "text");
        if (i == TextToSpeech.SUCCESS)
        {
            int resultRu = speech.isLanguageAvailable(Locale.getDefault());
            if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                installTTSData(true);
            }
            int resultEn = speech.isLanguageAvailable(Locale.US);
            if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                installTTSData(true);
            }
            else
            {
                speech.setLanguage(Locale.US);
                speech.speak("lets go",TextToSpeech.QUEUE_ADD,map);
            }
        }else
        {
            installTTSData(false);
        }

        speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String s)
            {

            }

            @Override
            public void onDone(String s)
            {
                speakDone();
            }

            @Override
            public void onError(String s)
            {

            }
        });
    }

    public void speak(String text)
    {
        HashMap<String, String> hash = new HashMap<String,String>();
        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
        speech.speak(text, TextToSpeech.QUEUE_ADD, hash);

    }

    public abstract void speakDone();
    public abstract void installTTSData(boolean result);
}
