package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.Word;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.interfaces.IModalFragment;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.main.SplashScreenActivity;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.settings.AppData;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by Renat
 */

@AndroidEntryPoint
public class ServiceActivity extends AppCompatActivity implements IModalFragment
{
    private boolean isServiceEnabled = false;
    public static IStopServiceByUser iStopServiceByUser;
    public static final String ARG_JSON = ServiceActivity.class.getCanonicalName() + ".ARG_JSON";
    private boolean stoppedByUser;

    @Override
    public void openApp()
    {
        stoppedByUser = true;
        finish();
        startActivity(new Intent(this, SplashScreenActivity.class));
    }

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

        Intent intent = new Intent(this, LexiconService.class);
        stopService(intent);

        MainViewModel vm = new ViewModelProvider(this).get(MainViewModel.class);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ServiceActivity.this);
        String preferencesString = preferences.getString(getString(R.string.key_list_display_mode), "0");
        String displayVariantStr = preferences.getString(getString(R.string.key_display_variant), "0");
        isServiceEnabled = preferences.getBoolean(getString(R.string.key_service), false);
        int displayMode = 0;
        if (preferencesString != null && displayVariantStr != null)
        {
            try
            {
                displayMode = Integer.parseInt(preferencesString);
            } catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
        }


        int finalDisplayMode = displayMode;

        vm.getWordCounters().observe(this, counters -> {
            if (counters != null && counters.size() > 1)
            {
                String json = getIntent().getStringExtra(ServiceActivity.ARG_JSON);
                if (json != null)
                {
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
                        ModalFragment modalFragment = ModalFragment.newInstance(json, countersList, this);
                        modalFragment.show(getSupportFragmentManager().beginTransaction(), ModalFragment.TAG);
                        vm.goForward(Arrays.asList(words));
                    }
                    else if (finalDisplayMode == 1)
                    {
                        TestModalFragment testModalFragment = TestModalFragment.newInstance(json, countersList, this);
                        testModalFragment.show(getSupportFragmentManager().beginTransaction(), TestModalFragment.TAG);
                    }
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
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (isServiceEnabled)
        {
            Intent intent = new Intent(this, LexiconService.class);
            if (!stoppedByUser)
            {
                startService(intent);
            }
            else
            {
                stopService(intent);
                stoppedByUser = false;
            }
        }
    }

    public void stopAppService()
    {
        if (iStopServiceByUser != null)
        {
            stoppedByUser = true;
            iStopServiceByUser.onStoppedByUser();
            new AlarmScheduler(this).cancel(AlarmScheduler.REQUEST_CODE, AlarmScheduler.REPEAT_SHOOT_ACTION);
        }
    }


}
