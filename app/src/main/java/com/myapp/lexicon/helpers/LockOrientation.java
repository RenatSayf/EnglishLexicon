package com.myapp.lexicon.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

/**
 * Created by Ренат on 05.08.2016.
 */
public class LockOrientation
{
    private Activity activity;

    public LockOrientation(Activity activity)
    {
        this.activity = activity;
    }

    @SuppressLint("InlinedApi")
    public void lock()
    {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (activity.getResources().getConfiguration().orientation)
        {
            case Configuration.ORIENTATION_PORTRAIT:
                if (rotation == android.view.Surface.ROTATION_90 || rotation == android.view.Surface.ROTATION_180)
                {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                }
                else
                {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
            {
                rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90)
                {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                else
                {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                }
            }
                break;
        }
    }

    public void unLock()
    {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
