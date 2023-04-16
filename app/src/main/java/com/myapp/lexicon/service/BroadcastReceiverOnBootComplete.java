package com.myapp.lexicon.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppSettings;


public class BroadcastReceiverOnBootComplete extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        AppSettings appSettings = new AppSettings(context);
        try
        {
            if (intent != null && intent.getAction() != null)
            {
                if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
                {
                    if (appSettings.getPlayList() != null && appSettings.getPlayList().size() > 0)
                    {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean isUseService = preferences.getBoolean(context.getString(R.string.key_service), true);
                        if (isUseService)
                        {

                        }
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}