package com.myapp.lexicon.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.myapp.lexicon.R;
import com.myapp.lexicon.main.MainActivity;
import com.myapp.lexicon.settings.AppData;

import static com.myapp.lexicon.main.MainActivity.serviceIntent;

/**
 * Created by Renat
 */

public class ServiceDialog extends AppCompatActivity
{
    private int displayVariant = 0;

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

        if (serviceMode == 0)
        {
            ModalFragment modalFragment = ModalFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, modalFragment).commit();
        }
        else if (serviceMode == 1)
        {
            TestModalFragment testModalFragment = TestModalFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_frame, testModalFragment).commit();
        }
        else return;

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
    protected void onDestroy()
    {
        if (displayVariant == 1)
        {
            if (serviceIntent == null)
            {
                MainActivity.serviceIntent = new Intent(this, LexiconService.class);
            }
            startService(MainActivity.serviceIntent);
        }
        super.onDestroy();
    }
}
