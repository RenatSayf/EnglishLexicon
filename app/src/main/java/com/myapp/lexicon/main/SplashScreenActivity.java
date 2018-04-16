package com.myapp.lexicon.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * The initial activity that checks the voice functions of the device
 */
public class SplashScreenActivity extends Activity
{
    public static TextToSpeech speech;
    public HashMap<String, String> map = new HashMap<>();

    private UpdateBroadcastReceiver broadcastReceiver; // TODO: UpdateBroadcastReceiver. 1 - объявление экземпляра UpdateBroadcastReceiver
    private Intent messageErrorIntent;  // TODO: UpdateBroadcastReceiver. 2 - объявление экземпляра Intent
    public String EXTRA_KEY_ERROR_MSG = "key_error_message";    // TODO: UpdateBroadcastReceiver. 3 - определение ключа для приемника
    public String EXTRA_KEY_MSG_ID = "key_msg_id";              // TODO: UpdateBroadcastReceiver. 3 - определение ключа для приемника
    public String ACTION_UPDATE = "com.myapp.lexicon.main.a_SplashScreenActivity";   // TODO: UpdateBroadcastReceiver. 4 - определения действия

    private AppSettings appSettings;
    private SharedPreferences preferences;
    private AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_layout_splash_screen);

        preferences = PreferenceManager.getDefaultSharedPreferences(SplashScreenActivity.this);
        int serviceMode = Integer.parseInt(preferences.getString(getString(R.string.key_list_display_mode), "0"));
        appSettings = new AppSettings(SplashScreenActivity.this);
        appData = AppData.getInstance();
        appData.setServiceMode(serviceMode);

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

        boolean isAppLang = checkAppLang();
        if (!isAppLang)
        {
            appSettings.setTranslateLang(appSettings.getTransLangList().get(0));
        }
        else
        {
            String language = getResources().getConfiguration().locale.getLanguage();
            String country = getResources().getConfiguration().locale.getCountry();
            String deviceLangCode = language.concat("_").concat(country);
            appSettings.setTranslateLang(deviceLangCode);
        }

        speech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                {
                    //int resultRu = speech.isLanguageAvailable(Locale.getDefault());
                    int resultRu = speech.isLanguageAvailable(new Locale(appSettings.getTranslateLang()));
                    if (resultRu == TextToSpeech.LANG_MISSING_DATA || resultRu == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Intent installTTSdata = new Intent();
                        installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        dialogErrorTTS(installTTSdata, Locale.getDefault().getDisplayCountry() + " not supported", true);
                        return;
                    }

                    int resultEn = speech.isLanguageAvailable(Locale.US);
                    if (resultEn == TextToSpeech.LANG_MISSING_DATA || resultEn == TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Intent installTTSdata = new Intent();
                        installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                        dialogErrorTTS(installTTSdata, getString(R.string.message_inst_tts_data), false);
                    }
                    else
                    {
                        boolean isStartSpeech = preferences.getBoolean("is_start_speech", true);
                        if (isStartSpeech)
                        {
                            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.US.getDisplayLanguage());
                            speech.setLanguage(Locale.US);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                            {
                                speech.speak(getString(R.string.start_speech_en), TextToSpeech.QUEUE_ADD, null, map.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                            } else
                            {
                                speech.speak(getString(R.string.start_speech_en),TextToSpeech.QUEUE_ADD,map);
                            }
                        } else
                        {
                            Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                            SplashScreenActivity.this.startActivity(intent);
                            SplashScreenActivity.this.finish();
                        }
                    }
                }else
                {
                    Intent instTTSengine = new Intent(Intent.ACTION_VIEW);
                    instTTSengine.setData(Uri.parse(getString(R.string.url_google_tts)));
                    dialogErrorTTS(instTTSengine, getString(R.string.message_inst_tts_engine), false);
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
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    SplashScreenActivity.this.startActivity(intent);
                    SplashScreenActivity.this.finish();
                }
            }

            @Override
            public void onDone(String utteranceId)
            {
                if (utteranceId.equals(Locale.US.getDisplayLanguage()))
                {
                    map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.getDefault().getDisplayLanguage());
                    //speech.setLanguage(Locale.getDefault());
                    speech.setLanguage(new Locale(appSettings.getTranslateLang()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        speech.speak(getString(R.string.start_speech_ru), TextToSpeech.QUEUE_ADD, null, map.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                    } else
                    {
                        speech.speak(getString(R.string.start_speech_ru), TextToSpeech.QUEUE_ADD, map);
                    }
                }
            }

            @Override
            public void onError(String utteranceId)
            {
                messageErrorIntent = new Intent();  // TODO: UpdateBroadcastReceiver. 8 - создание экземпляра Intent
                messageErrorIntent.setAction(ACTION_UPDATE);    // TODO: UpdateBroadcastReceiver. 9 - установка действия
                messageErrorIntent.addCategory(Intent.CATEGORY_DEFAULT);    // TODO: UpdateBroadcastReceiver. 10 - установка категории

                if (utteranceId.equals(Locale.US.getDisplayLanguage()))
                {
                    // TODO: UpdateBroadcastReceiver. 11 - кладем сообщение в Intent
                    messageErrorIntent.putExtra(EXTRA_KEY_MSG_ID, Locale.US.getDisplayLanguage());
                    // TODO: UpdateBroadcastReceiver. 12 - отправка сообщения приемнику
                    sendBroadcast(messageErrorIntent);
                }
                if (utteranceId.equals(Locale.getDefault().getDisplayLanguage()))
                {
                    messageErrorIntent.putExtra(EXTRA_KEY_MSG_ID, Locale.getDefault().getDisplayLanguage());
                    sendBroadcast(messageErrorIntent);
                }
            }
        });
    }

    private void dialogErrorTTS(final Intent intent, String message, boolean isContinue)
    {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setTitle(R.string.dialog_title_warning).setIcon(R.drawable.icon_warning)
                .setPositiveButton(R.string.btn_text_setup, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        startActivity(intent);
                        SplashScreenActivity.this.finish();
                    }
                });
        if (isContinue)
        {
            alertDialog.setNegativeButton(R.string.btn_text_continue, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    appSettings.setEnglishSpeechOnly(false);
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    SplashScreenActivity.this.startActivity(intent);
                    SplashScreenActivity.this.finish();
                }
            });
        }
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.create();
        alertDialog.show();
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
            String id = intent.getStringExtra(EXTRA_KEY_MSG_ID);

            Intent installTTSdata = new Intent();
            installTTSdata.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);

            // TODO: UpdateBroadcastReceiver. 14 - отображение сообщения пользователю... Уфф!!!
            if (id.equals(Locale.US.getDisplayLanguage()))
            {
                dialogErrorTTS(installTTSdata, getString(R.string.message_inst_tts_data), false);
            }
            if (id.equals(Locale.getDefault().getDisplayLanguage()))
            {
                if (appSettings.isEnglishSpeechOnly())
                {
                    dialogErrorTTS(installTTSdata, getString(R.string.message_inst_tts_data_ru), true);
                }
                else
                {
                    SplashScreenActivity.this.startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    SplashScreenActivity.this.finish();
                }
            }
        }
    }

    private boolean checkAppLang()
    {
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String deviceLangCode = language.concat("_").concat(country);
        ArrayList<String> appLangList = appSettings.getTransLangList();

        return appLangList.contains(deviceLangCode);
    }


}
