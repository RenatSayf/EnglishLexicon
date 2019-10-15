package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.myapp.lexicon.R;
import com.myapp.lexicon.main.MainActivity;
import com.myapp.lexicon.main.MainActivityOnStart;
import com.myapp.lexicon.settings.AppData;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Locale;

import static com.myapp.lexicon.main.MainActivity.serviceIntent;

/**
 * Created by Renat
 */

public class ServiceDialog extends AppCompatActivity
{
    private int displayVariant = 0;
    public static IStopServiceByUser iStopServiceByUser;
    public static TextToSpeech speech;
    public static HashMap<String, String> map = new HashMap<>();

    public interface IStopServiceByUser
    {
        void onStoppedByUser();
    }

    public static void setStoppedByUserListener(IStopServiceByUser listener)
    {
        iStopServiceByUser = listener;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_dialog_activity);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ServiceDialog.this);
        int serviceMode = 0;
        String preferencesString = preferences.getString(getString(R.string.key_list_display_mode), "0");
        String displayVariantStr = preferences.getString(getString(R.string.key_on_unbloking_screen), "0");
        if (preferencesString != null)
        {
            serviceMode = Integer.parseInt(preferencesString);
            if (displayVariantStr != null)
            {
                displayVariant = Integer.parseInt(displayVariantStr);
                displayVariant = Integer.parseInt(displayVariantStr);
            }
        }

        if (displayVariant == 1 && serviceIntent != null)
        {
            stopService(serviceIntent);
        }

        speech = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS )
                {
                    int resultEn = speech.isLanguageAvailable(Locale.US);
                    if (resultEn == TextToSpeech.LANG_COUNTRY_AVAILABLE)
                    {
                        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, Locale.US.getDisplayLanguage());
                        speech.setLanguage(Locale.US);
                        speech.stop();
                    }
                }
                if (status == TextToSpeech.LANG_NOT_SUPPORTED || status == TextToSpeech.LANG_MISSING_DATA)
                {
                    stopService(serviceIntent);
                }
            }
        });

        if (serviceMode == 0)
        {
            ModalFragment modalFragment = ModalFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, modalFragment).commit();
        } else if (serviceMode == 1)
        {
            TestModalFragment testModalFragment = TestModalFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, testModalFragment).commit();
        } else return;

        AppData appData = AppData.getInstance();

        int count = appData.getUnLookPhoneCount();
        count++;
        appData.setUnLookPhoneCount(count);

        if (appData.isAdMob())
        {
            if (appData.isOnline(this) && appData.getUnLookPhoneCount() > 2)
            {
                if (savedInstanceState == null)
                {
                    appData.setUnLookPhoneCount(0);
                    ModalBannerFragment bannerFragment = new ModalBannerFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.banner_frame_service, bannerFragment).commit();
                }
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onPause()
    {
        if (displayVariant == 1 && !LexiconService.stopedByUser)
        {
            if (serviceIntent == null)
            {
                MainActivity.serviceIntent = new Intent(this, LexiconService.class);
            }
            startService(MainActivity.serviceIntent);
        }
        if (LexiconService.stopedByUser)
        {
            if (serviceIntent == null)
            {
                MainActivity.serviceIntent = new Intent(this, LexiconService.class);
            }
            LexiconService.stopedByUser = false;

        }
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainActivityStart(MainActivityOnStart event)
    {
        if (displayVariant == 1 && !LexiconService.stopedByUser)
        {
            Intent serviceIntent = event.intent;
            if (serviceIntent != null)
            {
                stopService(serviceIntent);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStoppedServiceByUser(StopedServiceByUserEvent event)
    {
        if (iStopServiceByUser != null)
        {
            iStopServiceByUser.onStoppedByUser();
        }
        finish();
    }

}
