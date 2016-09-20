package com.myapp.lexicon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Ренат on 15.09.2016.
 */
public class a_SplashScreenActivity extends Activity
{
    public static z_Speaker speaker;

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
                        Toast.makeText(a_SplashScreenActivity.this,"Установите голосовые функции",Toast.LENGTH_SHORT).show();
                        Intent install = new Intent();
                        install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        startActivity(install);
                    }
                    else
                    {
                        speech.setLanguage(Locale.US);
                        speech.speak("lets go",TextToSpeech.QUEUE_ADD,map);
                    }
                }else
                {

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

            }
        });

//        speaker = new z_Speaker(this)
//        {
//            @Override
//            public void speakDone()
//            {
//                Intent intent = new Intent(a_SplashScreenActivity.this, a_MainActivity.class);
//                a_SplashScreenActivity.this.startActivity(intent);
//                a_SplashScreenActivity.this.finish();
//            }
//
//            @Override
//            public void installTTSData(boolean result)
//            {
//                if (result)
//                {
//                    Toast.makeText(a_SplashScreenActivity.this,"Установите голосовые функции",Toast.LENGTH_SHORT).show();
//                    Intent install = new Intent();
//                    install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                    startActivity(install);
//                }
//                else
//                {
//                    a_SplashScreenActivity.this.finish();
//                }
//            }
//        };





    }
}
