package com.myapp.lexicon.main;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.myapp.lexicon.R;

import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.NonNull;

public class Speaker
{

    private final TextToSpeech speaker;

    @NonNull
    private final Listener listener;

    private final Context context;
    private Locale localeRu;
    private boolean isSpeech = true;
    private boolean isEnSpeech = true;
    private boolean isRuSpeech = true;

    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
    public Speaker(Context context, @NonNull Listener listener)
    {
        this.context = context;
        this.listener = listener;
        speaker = new TextToSpeech(context, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                speechInit(status);
            }
        });
    }

    public interface Listener
    {
        void onSuccessInit();
        void onSpeechStart(String id);
        void onSpeechDone(String id);
        void onSpeechError(String id);
        void onSpeechInitNotSuccess(int status);
        void onEngLangNotSupported(int status);
        void onRusLangNotSupported(int status);
    }

    private void speechInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            localeRu = new Locale(this.context.getString(R.string.lang_code_translate));

            int resultRu = this.speaker.isLanguageAvailable(localeRu);
            if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                isRuSpeech = false;
                listener.onRusLangNotSupported(resultRu);
                return;
            }

            int resultEn = this.speaker.isLanguageAvailable(Locale.US);
            if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                isEnSpeech = false;
                listener.onEngLangNotSupported(resultEn);
            }
            else {
                listener.onSuccessInit();
            }
        }
        else
        {
            isSpeech = false;
            listener.onSpeechInitNotSuccess(status);
        }
    }

    public int doSpeech(String text, Locale locale)
    {
        int speakResult = Integer.MIN_VALUE;
        if (isSpeech)
        {
            HashMap<String, String> utterance_Id = new HashMap<>();
            if (locale.equals(Locale.US))
            {
                if (isEnSpeech)
                {
                    utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "En");
                } else
                {
                    return speakResult;
                }
            }

            if (locale.equals(localeRu))
            {
                if (isRuSpeech)
                {
                    utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Ru");
                } else
                {
                    return speakResult;
                }
            }
            int supportCode;
            supportCode = speaker.setLanguage(locale);

            if (supportCode >= 0)
            {
                speakResult = speaker.speak(text, TextToSpeech.QUEUE_FLUSH, null, utterance_Id.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
            }
        }

        speaker.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String id)
            {
                if (listener != null)
                {
                    listener.onSpeechStart(id);
                }
            }

            @Override
            public void onDone(String id)
            {
                if (listener != null)
                {
                    listener.onSpeechDone(id);
                }
            }

            @Override
            public void onError(String id)
            {
                if (listener != null)
                {
                    listener.onSpeechError(id);
                }
            }
        });
        return speakResult;
    }

    public void shutdown()
    {
        speaker.shutdown();
    }

    public int stop()
    {
        return speaker.stop();
    }

}
