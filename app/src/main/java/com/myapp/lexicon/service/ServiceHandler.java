package com.myapp.lexicon.service;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by Renat
 */

public class ServiceHandler extends Handler
{
    private WeakReference<LexiconService> wrActivity;

    public ServiceHandler(LexiconService activity)
    {
        wrActivity = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg)
    {
        super.handleMessage(msg);
        LexiconService activity = wrActivity.get();
        if (activity != null)
        {
            showActivity(activity);
        }
    }

    private void showActivity(LexiconService activity)
    {
        Intent intent = new Intent("android.intent.action.lexicon.ServiceDialog");
        intent.setClass(activity, ServiceDialog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.putExtra("text", "Hello!");
        activity.startActivity(intent);
    }
}
