package com.myapp.lexicon.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.myapp.lexicon.R;
import com.myapp.lexicon.p_PlayList;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Renat on 27.02.2017.
 */

public class AppSettings
{
    private Context context;

    private String KEY_ENG_ONLY = "eng_only";
    private String KEY_PLAY_LIST = "play_list";
    private String KEY_PLAY_LIST_ITEMS = "play_list_items";

    public AppSettings(Context context)
    {
        this.context = context;
    }

    /**
     *  Set the setting of english speech synthesis only, either no
     * @param isEngOnly    true - set only english speech or false - set english and default speech
     */
    public void setEnglishSpeechOnly(boolean isEngOnly)
    {
        if (isEngOnly)
        {
            context.getSharedPreferences(KEY_ENG_ONLY, MODE_PRIVATE).edit().putBoolean(KEY_ENG_ONLY, true).apply();
        } else
        {
            context.getSharedPreferences(KEY_ENG_ONLY, MODE_PRIVATE).edit().putBoolean(KEY_ENG_ONLY, false).apply();
        }
    }

    /**
     *
     * @return  true if set english speech only otherwise false
     */
    public boolean isEnglishSpeechOnly()
    {
        return context.getSharedPreferences(KEY_ENG_ONLY, MODE_PRIVATE).getBoolean(KEY_ENG_ONLY, false);
    }

    public void savePlayList(ArrayList<String> list)
    {
        String play_list_string = "";
        for (String item : list)
        {
            play_list_string += item + " ";
        }
        play_list_string.trim();
        context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putString(KEY_PLAY_LIST_ITEMS, play_list_string).apply();
    }


}
