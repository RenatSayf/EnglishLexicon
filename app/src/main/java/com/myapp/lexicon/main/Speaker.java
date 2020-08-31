package com.myapp.lexicon.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppSettings;

import java.util.HashMap;
import java.util.Locale;

public class Speaker extends TextToSpeech
{
    private AppSettings appSettings;

    public Speaker(final Activity activity, OnInitListener listener)
    {
        super(activity, listener);
        appSettings = new AppSettings(activity);
    }

    public void speechInit(int status, Activity activity, TextToSpeech speaker)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            int resultRu = speaker.isLanguageAvailable(new Locale(appSettings.getTranslateLang()));
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

    private void dialogErrorTTS(final Activity activity, final Intent intent, String message, boolean isContinue)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity).setTitle(R.string.dialog_title_warning).setIcon(R.drawable.icon_warning)
                .setPositiveButton(R.string.btn_text_setup, (dialog, which) -> {
                    activity.startActivity(intent);
                    activity.finish();
                });
        if (isContinue)
        {
            alertDialog.setNegativeButton(R.string.btn_text_continue, (dialog, which) -> {
                appSettings.setEnglishSpeechOnly(false);
                Intent intent1 = new Intent(activity, MainActivity.class);
                activity.startActivity(intent1);
                activity.finish();
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
        utterance_Id.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "translate_dialog");
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
    }
}
