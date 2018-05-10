package com.myapp.lexicon.service;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppData;

/**
 * Created by Renat
 */

public class ServiceDialog extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_dialog_activity);

        int serviceMode = AppData.getInstance().getServiceMode();

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

        if (AppData.getInstance().isAdMob())
        {
            if (AppData.getInstance().isOnline(this))
            {
                if (savedInstanceState == null)
                {
                    ModalBannerFragment bannerFragment = new ModalBannerFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.banner_frame_service, bannerFragment).commit();
                }
            }
        }
    }


}
