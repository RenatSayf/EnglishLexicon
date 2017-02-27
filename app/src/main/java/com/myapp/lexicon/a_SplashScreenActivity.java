package com.myapp.lexicon;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ренат on 15.09.2016.
 */
public class a_SplashScreenActivity extends Activity
{
    public static TextToSpeech speech;
    public static HashMap<String, String> map = new HashMap<String, String>();

    private TextView textViewError;

    private UpdateBroadcastReceiver broadcastReceiver; // TODO: UpdateBroadcastReceiver. 1 - объявление экземпляра UpdateBroadcastReceiver
    private Intent messageErrorIntent;  // TODO: UpdateBroadcastReceiver. 2 - объявление экземпляра Intent
    public String EXTRA_KEY_ERROR_MSG = "key_error_message";    // TODO: UpdateBroadcastReceiver. 3 - определение ключа для приемника
    public String ACTION_UPDATE = "com.myapp.lexicon.a_SplashScreenActivity";   // TODO: UpdateBroadcastReceiver. 4 - определения действия

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_layout_splash_screen);

        textViewError = (TextView) findViewById(R.id.text_view_error);

        //region TODO: UpdateBroadcastReceiver. 5 - Регистрируем приёмник
        broadcastReceiver = new UpdateBroadcastReceiver();
        IntentFilter updateIntentFilter = new IntentFilter(ACTION_UPDATE);
        updateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        try
        {
            registerReceiver(broadcastReceiver, updateIntentFilter);
        } catch (Exception e)
        {
            e.printStackTrace();
            finish();
        }
        //endregion

        speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                {

//                    int resultRu = speech.isLanguageAvailable(Locale.getDefault());
//                    if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
//                    {
//                        Intent installTTSdata = new Intent();
//                        installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//                        dialogErrorTTS(installTTSdata, R.string.message_inst_tts_data_ru);
//                    }
                    int resultEn = speech.isLanguageAvailable(Locale.US);
                    if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Intent installTTSdata = new Intent();
                        installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        dialogErrorTTS(installTTSdata, R.string.message_inst_tts_data);
                    }
                    else
                    {
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.US.getDisplayLanguage());
                        speech.setLanguage(Locale.US);
                        speech.speak("lets go",TextToSpeech.QUEUE_ADD,map);
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
                if (utteranceId.equals(Locale.getDefault().getDisplayLanguage()))
                {
                    Intent intent = new Intent(a_SplashScreenActivity.this, a_MainActivity.class);
                    a_SplashScreenActivity.this.startActivity(intent);
                    a_SplashScreenActivity.this.finish();
                }
            }

            @Override
            public void onDone(String utteranceId)
            {
                if (utteranceId.equals(Locale.US.getDisplayLanguage()))
                {
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.getDefault().getDisplayLanguage());
                    speech.setLanguage(Locale.getDefault());
                    speech.speak("Поехали",TextToSpeech.QUEUE_ADD,map);
                }
            }

            @Override
            public void onError(String utteranceId)
            {
                Intent installTTSdata = new Intent();
                installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);

                messageErrorIntent = new Intent();  // TODO: UpdateBroadcastReceiver. 8 - создание экземпляра Intent
                messageErrorIntent.setAction(ACTION_UPDATE);    // TODO: UpdateBroadcastReceiver. 9 - установка действия
                messageErrorIntent.addCategory(Intent.CATEGORY_DEFAULT);    // TODO: UpdateBroadcastReceiver. 10 - установка категории

                if (utteranceId.equals(Locale.US.getDisplayLanguage()))
                {
                    // TODO: UpdateBroadcastReceiver. 11 - кладем сообщение в Intent
                    messageErrorIntent.putExtra(EXTRA_KEY_ERROR_MSG, getString(R.string.message_inst_tts_data));
                    // TODO: UpdateBroadcastReceiver. 12 - отправка сообщения приемнику
                    sendBroadcast(messageErrorIntent);
                }
                if (utteranceId.equals(Locale.getDefault().getDisplayLanguage()))
                {
                    messageErrorIntent.putExtra(EXTRA_KEY_ERROR_MSG, getString(R.string.message_inst_tts_data_ru));
                    sendBroadcast(messageErrorIntent);
                }
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);  // TODO: UpdateBroadcastReceiver. 7 - снятие регистрации приемника
    }

    // TODO: UpdateBroadcastReceiver. 6 - определение класса UpdateBroadcastReceiver
    public class UpdateBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // TODO: UpdateBroadcastReceiver. 13 - получение сообщения
            String errorTxt = intent.getStringExtra(EXTRA_KEY_ERROR_MSG);
            // TODO: UpdateBroadcastReceiver. 14 - отображение сообщения пользователю... Уфф!!!
            textViewError.setText(errorTxt);
        }
    }


}
