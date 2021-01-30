package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.Word;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.settings.AppData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by Renat
 */

@AndroidEntryPoint
public class ServiceActivity extends AppCompatActivity
{
    private boolean isServiceEnabled = false;
    public static IStopServiceByUser iStopServiceByUser;
    public static TextToSpeech speech;
    public static HashMap<String, String> map = new HashMap<>();
    public static final String ARG_JSON = ServiceActivity.class.getCanonicalName() + ".ARG_JSON";
    public static final String ARG_COUNTERS = ServiceActivity.class.getCanonicalName() + ".ModalFragment.arg_counters";

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

        MainViewModel vm = new ViewModelProvider(this).get(MainViewModel.class);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ServiceActivity.this);
        String preferencesString = preferences.getString(getString(R.string.key_list_display_mode), "0");
        String displayVariantStr = preferences.getString(getString(R.string.key_display_variant), "0");
        isServiceEnabled = preferences.getBoolean(getString(R.string.key_service), false);
        int displayMode = 0;
        int displayVariant = 0;
        if (preferencesString != null && displayVariantStr != null)
        {
            try
            {
                displayMode = Integer.parseInt(preferencesString);
                displayVariant = Integer.parseInt(displayVariantStr);
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
        }

        speech = new TextToSpeech(this, status -> {
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
                stopService(new Intent(this, LexiconService.class));
            }
        });

        int finalDisplayMode = displayMode;
        int finalDisplayVariant = displayVariant;

        vm.getWordCounters().observe(this, counters -> {
            if (counters != null && counters.size() > 1)
            {
                String json = getIntent().getStringExtra(LexiconService.Companion.getARG_JSON());
                Word[] words = StringOperations.getInstance().jsonToWord(json);
                ArrayList<Integer> countersList = new ArrayList<>();
                if (words.length > 0)
                {
                    int id = words[0].get_id();
                    int index = counters.indexOf(id);
                    countersList.add(counters.get(0));
                    countersList.add(counters.size() - 1);
                    countersList.add(index);
                    counters.clear();
                }
                if (finalDisplayMode == 0)
                {
                    ModalFragment modalFragment = ModalFragment.newInstance(json, countersList);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, modalFragment).commit();
                }
                else if (finalDisplayMode == 1)
                {
                    TestModalFragment testModalFragment = TestModalFragment.newInstance(json, countersList);
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, testModalFragment).commit();
                }
                if (finalDisplayVariant == 1)
                {
                    stopService(new Intent(this, LexiconService.class));
                    stopAppService();
                }
            }


        });

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
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onDetachedFromWindow()
    {
        if (isServiceEnabled)
        {
            if (!LexiconService.stopedByUser)
            {
                Intent intent = new Intent(this, LexiconService.class);
                startService(intent);
            }
            if (LexiconService.stopedByUser)
            {
//                if (serviceIntent == null)
//                {
//                    MainActivity.serviceIntent = new Intent(this, LexiconService.class);
//                }
                LexiconService.stopedByUser = false;
            }
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDestroy()
    {
        if (speech != null)
        {
            speech.shutdown();
        }
        super.onDestroy();
    }

    public void stopAppService()
    {
        if (iStopServiceByUser != null)
        {
            LexiconService.stopedByUser = true;
            iStopServiceByUser.onStoppedByUser();
            new AlarmScheduler(this).cancel(AlarmScheduler.REQUEST_CODE, AlarmScheduler.REPEAT_SHOOT_ACTION);
        }
        //finish();
    }

}
