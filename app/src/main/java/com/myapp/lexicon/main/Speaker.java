package com.myapp.lexicon.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.myapp.lexicon.R;

import java.util.HashMap;
import java.util.Locale;

public class Speaker
{
    public static final String ARG_SETUP = "SETUP";
    public static final String ARG_CONTINUE_WITHOUT = "CONTINUE_WITHOUT";

    private final TextToSpeech speaker;
    private final IOnSpeechListener listener;
    private final Context context;
    private Locale localeRu;
    private boolean isSpeech = true;
    private boolean isEnSpeech = true;
    private boolean isRuSpeech = true;

    @SuppressWarnings("Convert2Lambda")
    public Speaker(Context context, IOnSpeechListener listener)
    {
        this.context = context;
        this.listener = listener;
        speaker = new TextToSpeech(context, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                speechInit(status, context, null);
            }
        });
    }

    public interface IOnSpeechListener
    {
        void onSpeechStart(String id);
        void onSpeechDone(String id);
        void onSpeechError(String id);
        void onContinued(String arg);
        void onSpeechInitNotSuccess(int status);
        void onEngLangNotSupported(int status);
        void onRusLangNotSupported(int status);
    }

    public void speechInit(int status, Context context, TextToSpeech speaker)
    {
        if (status == TextToSpeech.SUCCESS)
        {

            localeRu = new Locale(this.context.getString(R.string.lang_code_translate));

            int resultRu = this.speaker.isLanguageAvailable(localeRu);
            if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                if (listener != null)
                {
                    isRuSpeech = false;
                    listener.onRusLangNotSupported(resultRu);
                }
//                Intent installTTSdata = new Intent();
//                installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                dialogErrorTTS(this.context, installTTSdata, Locale.getDefault().getDisplayCountry() + " not supported", true);
            }

            int resultEn = this.speaker.isLanguageAvailable(Locale.US);
            if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                if (listener != null)
                {
                    isEnSpeech = false;
                    listener.onEngLangNotSupported(resultEn);
                }
//                Intent installTTSdata = new Intent();
//                installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                dialogErrorTTS(this.context, installTTSdata, this.context.getString(R.string.message_inst_tts_data), false);
            }
        }
        else
        {
            if (listener != null)
            {
                isSpeech = false;
                listener.onSpeechInitNotSuccess(status);
            }
//            Intent instTTSengine = new Intent(Intent.ACTION_VIEW);
//            instTTSengine.setData(Uri.parse(this.context.getString(R.string.url_google_tts)));
//            dialogErrorTTS(this.context, instTTSengine, this.context.getString(R.string.message_inst_tts_engine), false);
        }
    }

    private void dialogErrorTTS(final Context context, final Intent intent, String message, boolean isContinue)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context).setTitle(R.string.dialog_title_warning).setIcon(R.drawable.icon_warning)
                .setPositiveButton(R.string.btn_text_setup, (dialog, which) -> {
                    context.startActivity(intent);
                    if (listener != null)
                    {
                        listener.onContinued(ARG_SETUP);
                    }
                });
        if (isContinue)
        {
            alertDialog.setNegativeButton(R.string.btn_text_continue, (dialog, which) -> {
                if (listener != null)
                {
                    listener.onContinued(ARG_CONTINUE_WITHOUT);
                }
            });
        }
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.create();
        alertDialog.show();
    }

    public int doSpeech(String text, Locale locale)
    {
        int speakResult = Integer.MIN_VALUE;
        HashMap<String, String> utterance_Id = new HashMap<>();
        utterance_Id.clear();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {

                speakResult = speaker.speak(text, TextToSpeech.QUEUE_FLUSH, null, utterance_Id.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
            } else
            {
                speakResult = speaker.speak(text, TextToSpeech.QUEUE_FLUSH, utterance_Id);
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
