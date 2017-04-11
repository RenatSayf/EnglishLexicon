package com.myapp.lexicon.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.myapp.lexicon.wordstests.FindPairFragment;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 *
 * Helper class for work with the SharedPreferences
 */

public class AppSettings
{
    private Context context;

    private final String KEY_ENG_ONLY = "eng_only";
    private final String KEY_PLAY_LIST = "play_list";
    private final String KEY_PLAY_LIST_ITEMS = "play_list_items";
    private final String KEY_ORDER_PLAY = "order_play";
    private final String KEY_N_DICT = "N_dict";
    private final String KEY_N_WORD = "N_word";
    private final String KEY_CURRENT_DICT = "current_dict";
    private final String KEY_IS_PAUSE = "is_pause";

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
            AppData2 appData2 = AppData2.getInstance();

            if (appData2.getNdict() == position)
            {
                appData2.setNdict(appData2.getNdict()-1);
                if (appData2.getNdict() < 0)
                {
                    appData2.setNdict(0);
                }
                appData2.setNword(1);
            }
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
            int indexOf = playList.indexOf(item);
            if (playList.contains(item))
            {
                playList.remove(item);
                savePlayList(playList);
            }
            AppData2 appData2 = AppData2.getInstance();
            if (appData2.getNdict() == indexOf)
            {
                appData2.setNdict(appData2.getNdict()-1);
                if (appData2.getNdict() < 0)
                {
                    appData2.setNdict(0);
                }
                appData2.setNword(1);
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

    public void setWordNumber(int number)
    {
        context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putInt(KEY_N_WORD, number).apply();
    }

    public int getWordNumber()
    {
        return context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).getInt(KEY_N_WORD, 1);
    }

    public void setDictNumber(int number)
    {
        context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putInt(KEY_N_DICT, number).apply();
    }

    public int getDictNumber()
    {
        return context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).getInt(KEY_N_DICT, 0);
    }

    public void setCurrentDict(String name)
    {
        context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putString(KEY_CURRENT_DICT, name).apply();
    }

    public String getCurrentDict()
    {
        return context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).getString(KEY_CURRENT_DICT, null);
    }

    public void setPause(boolean param)
    {
        context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putBoolean(KEY_IS_PAUSE, param).apply();
    }

    public boolean isPause()
    {
        return context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).getBoolean(KEY_IS_PAUSE, false);
    }

    public final String KEY_SPINN_SELECT_ITEM = "key_spinn_select_item";
    public final String KEY_WORD_INDEX = "key_word_index";
    public final String KEY_COUNTER_RIGHT_ANSWER = "key_counter_right_answer";

    public void saveTestFragmentState(String tag, Bundle bundle)
    {
        SharedPreferences.Editor settingsEditor = context.getSharedPreferences(tag, MODE_PRIVATE).edit();
        if (bundle != null)
        {
            settingsEditor.putString(KEY_SPINN_SELECT_ITEM, bundle.getString(KEY_SPINN_SELECT_ITEM));
            settingsEditor.putInt(KEY_WORD_INDEX, bundle.getInt(KEY_WORD_INDEX));
            settingsEditor.putInt(KEY_COUNTER_RIGHT_ANSWER, bundle.getInt(KEY_COUNTER_RIGHT_ANSWER));
        } else
        {
            settingsEditor.remove(KEY_SPINN_SELECT_ITEM);
            settingsEditor.remove(KEY_WORD_INDEX);
            settingsEditor.remove(KEY_COUNTER_RIGHT_ANSWER);
        }
        settingsEditor.apply();
    }

    public Bundle getTestFragmentState(String tag)
    {
        Bundle bundle = new Bundle();
        SharedPreferences sharedPreferences = context.getSharedPreferences(tag, MODE_PRIVATE);
        bundle.putString(KEY_SPINN_SELECT_ITEM, sharedPreferences.getString(KEY_SPINN_SELECT_ITEM, null));
        bundle.putInt(KEY_WORD_INDEX, sharedPreferences.getInt(KEY_WORD_INDEX, 1));
        bundle.putInt(KEY_COUNTER_RIGHT_ANSWER, sharedPreferences.getInt(KEY_COUNTER_RIGHT_ANSWER, 0));

        return bundle;
    }


}
