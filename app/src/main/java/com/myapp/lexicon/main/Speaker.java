package com.myapp.lexicon.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.myapp.lexicon.R;

import java.util.HashMap;
import java.util.Locale;

public class Speaker extends TextToSpeech
{
    public static final String ARG_SETUP = "SETUP";
    public static final String ARG_CONTINUE_WITHOUT = "CONTINUE_WITHOUT";
    private IOnSpeechListener listener;

    public Speaker(final Context context, OnInitListener listener)
    {
        super(context, listener);
    }

    public interface IOnSpeechListener
    {
        void onSpeechStart(String id);
        void onSpeechDone(String id);
        void onSpeechError(String id);
        void onContinued(String arg);
    }

    public void setOnSpeechListener(IOnSpeechListener listener)
    {
        this.listener = listener;
    }

    public void speechInit(int status, Context activity, TextToSpeech speaker)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            int resultRu = speaker.isLanguageAvailable(Locale.getDefault());
            if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Intent installTTSdata = new Intent();
                installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                dialogErrorTTS(activity, installTTSdata, Locale.getDefault().getDisplayCountry() + " not supported", true);
                return;
            }

            int resultEn = speaker.isLanguageAvailable(Locale.US);
            if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Intent installTTSdata = new Intent();
                installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                dialogErrorTTS(activity, installTTSdata, activity.getString(R.string.message_inst_tts_data), false);
            }
        }
        else
        {
            Intent instTTSengine = new Intent(Intent.ACTION_VIEW);
            instTTSengine.setData(Uri.parse(activity.getString(R.string.url_google_tts)));
            dialogErrorTTS(activity, instTTSengine, activity.getString(R.string.message_inst_tts_engine), false);
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

    public void doSpeech(String text, Locale locale)
    {
        HashMap<String, String> utterance_Id = new HashMap<>();
        int supportCode;
        supportCode = this.setLanguage(locale);
        utterance_Id.clear();
        if (locale.equals(Locale.US))
        {
            utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "En");
        }
        if (locale.equals(Locale.getDefault()))
        {
            utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "Ru");
        }

        if (supportCode >= 0)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                this.speak(text, TextToSpeech.QUEUE_ADD, null, utterance_Id.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
            } else
            {
                this.speak(text, TextToSpeech.QUEUE_ADD, utterance_Id);
            }
        }

        this.setOnUtteranceProgressListener(new UtteranceProgressListener()
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
    }
}
