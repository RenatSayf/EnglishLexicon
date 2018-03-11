package com.myapp.lexicon.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.myapp.lexicon.settings.AppSettings;

/**
 * Created by Renat
 */

public class BroadcastReceiverOnBootComplete extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        AppSettings appSettings = new AppSettings(context);
        try
        {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
            {
                if (appSettings.getPlayList() != null && appSettings.getPlayList().size() > 0)
                {
                    Intent serviceIntent = new Intent(context, LexiconService.class);
                    context.startService(serviceIntent);
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
