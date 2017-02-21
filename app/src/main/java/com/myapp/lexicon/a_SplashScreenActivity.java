package com.myapp.lexicon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * Created by Ренат on 15.09.2016.
 */
public class a_SplashScreenActivity extends Activity
{
    public static TextToSpeech speech;
    public static HashMap<String, String> map = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_layout_splash_screen);

        speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "text");
                if (status == TextToSpeech.SUCCESS)
                {
                    int resultEn = speech.isLanguageAvailable(Locale.US);
                    if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Intent installTTSdata = new Intent();
                        installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        dialogErrorTTS(installTTSdata, R.string.message_inst_tts_data);
                    }
                    else
                    {
                        speech.setLanguage(Locale.US);
                        speech.speak("lets go",TextToSpeech.QUEUE_ADD,map);
                        //z_speechService2.setSpeechReferens(speech);
                    }
                }else
                {
                    Intent instTTSengine = new Intent(Intent.ACTION_VIEW);
                    instTTSengine.setData(Uri.parse(getString(R.string.url_google_tts)));
                    dialogErrorTTS(instTTSengine, R.string.message_inst_tts_engine);
                }
            }
        });

        speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String utteranceId)
            {
                Intent intent = new Intent(a_SplashScreenActivity.this, a_MainActivity.class);
                a_SplashScreenActivity.this.startActivity(intent);
                a_SplashScreenActivity.this.finish();
            }

            @Override
            public void onDone(String utteranceId)
            {

            }

            @Override
            public void onError(String utteranceId)
            {
                a_SplashScreenActivity.this.finish();
            }
        });
    }

    private void dialogErrorTTS(final Intent intent, int message)
    {
        new AlertDialog.Builder(this).setTitle("Предупреждение:").setIcon(R.drawable.icon_warning)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        startActivity(intent);
                        a_SplashScreenActivity.this.finish();
                    }
                })
                .setMessage(message)
                .create()
                .show();
    }


}
