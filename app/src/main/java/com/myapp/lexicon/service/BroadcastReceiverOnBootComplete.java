package com.myapp.lexicon.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BroadcastReceiverOnBootComplete extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            if (intent != null && intent.getAction() != null)
            {
                if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
                {

                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}