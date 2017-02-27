package com.myapp.lexicon.settings;

import android.content.Context;

/**
 * Created by Renat on 27.02.2017.
 */

public class AppSettings
{
    private Context context;

    private String KEY_ENG_ONLY = "eng_only";

    public AppSettings(Context context)
    {
        this.context = context;
    }

    public void setEnglishSpeechOnly(boolean isEngOnly)
    {
        if (isEngOnly)
        {
            context.getSharedPreferences(KEY_ENG_ONLY, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENG_ONLY, true).apply();
        } else
        {
            context.getSharedPreferences(KEY_ENG_ONLY, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENG_ONLY, false).apply();
        }
    }

    public boolean isEnglishSpeechOnly()
    {
        return context.getSharedPreferences(KEY_ENG_ONLY, Context.MODE_PRIVATE).getBoolean(KEY_ENG_ONLY, false);
    }


}
