package com.myapp.lexicon.appindex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.appindexing.FirebaseAppIndex;

/**
 * Created by Renat.
 */

public class AppIndexingUpdateReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent != null && FirebaseAppIndex.ACTION_UPDATE_INDEX.equals(intent.getAction()))
        {
            AppIndexingUpdateService.enqueueWork(context);
            context.startService(new Intent(context, AppIndexingService.class));
        }
    }
}
