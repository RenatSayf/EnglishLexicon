package com.myapp.lexicon.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.myapp.lexicon.R;
import com.myapp.lexicon.p_PlayList;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Renat on 27.02.2017.
 * Helper class for work with the SharedPreferences
 */

public class AppSettings
{
    private Context context;

    private String KEY_ENG_ONLY = "eng_only";
    private String KEY_PLAY_LIST = "play_list";
    private String KEY_PLAY_LIST_ITEMS = "play_list_items";
    private String KEY_ORDER_PLAY = "order_play";

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

    /**
     * Save the ArrayList<String>, converting it to the String
     * @param list (ArrayList)
     */
    public void savePlayList(ArrayList<String> list)
    {
        if (list != null && list.size() > 0)
        {
            String play_list_string = "";
            for (String item : list)
            {
                play_list_string += item + " ";
            }
            String temp = play_list_string.trim();
            context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putString(KEY_PLAY_LIST_ITEMS, temp).apply();
        }
    }

    /**
     * Remove one item of ArrayList and retain it state in the SharedPreferences
     * @param list ArrayList<String>
     * @param position  int
     */
    public void removeItemFromPlayList(ArrayList<String> list, int position)
    {
        if (list != null && list.size() > 0 && position >= 0 && position < list.size() )
        {
            list.remove(position);
            String play_list_string = "";
            for (String item : list)
            {
                play_list_string += item + " ";
            }
            String temp = play_list_string.trim();
            context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putString(KEY_PLAY_LIST_ITEMS, temp).apply();
        }
    }

    /**
     * Remove one item of ArrayList and retain it state in the SharedPreferences
     * @param item  String
     */
    public void removeItemFromPlayList(String item)
    {
        if (item != null)
        {
            ArrayList<String> playList = getPlayList();
            if (playList.contains(item))
            {
                playList.remove(item);
                savePlayList(playList);
            }
        }
    }

    /**
     * gets ArrayList from SharedPreferences
     * @return  ArrayList(String)
     */
    public ArrayList<String> getPlayList()
    {
        ArrayList<String> list = new ArrayList<>();
        String play_list_items = context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).getString(KEY_PLAY_LIST_ITEMS, null);

        if (play_list_items != null && play_list_items.length() > 0)
        {
            String[] splitArray = play_list_items.split(" ");
            for (int i = 0; i < splitArray.length; i++)
            {
                list.add(i, splitArray[i]);
            }
        }
        return list;
    }

    /**
     *
     * @param order  int
     */
    public void setOrderPlay(int order)
    {
        context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putInt(KEY_ORDER_PLAY, order).apply();
    }

    public int getOrderPlay()
    {
        return context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).getInt(KEY_ORDER_PLAY, 0);
    }


}
