package com.myapp.lexicon.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.ObjectSerializer;

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
    private final String KEY_ENG_ONLY_MODAL = "eng_only_modal";
    private final String KEY_PLAY_LIST = "play_list";
    private final String KEY_PLAY_LIST_ITEMS = "play_list_items";
    private final String KEY_ORDER_PLAY = "order_play";
    private final String KEY_N_DICT = "N_dict";
    private final String KEY_N_WORD = "N_word";
    private final String KEY_CURRENT_DICT = "current_dict";
    private final String KEY_IS_PAUSE = "is_pause";
    private final String KEY_TRANSLATE_LANG = "translate_lang";
    private final String KEY_TRANS_LANG_LIST = "trans_lang_list";

    private ArrayList<String> transLangList;

    public AppSettings(Context context)
    {
        this.context = context;
        transLangList = new ArrayList<>();
        transLangList.add(context.getString(R.string.lang_code_ru));
        transLangList.add(context.getString(R.string.lang_code_uk));
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
        return context.getSharedPreferences(KEY_ENG_ONLY, MODE_PRIVATE).getBoolean(KEY_ENG_ONLY, true);
    }

    /**
     * set the russian speech in the modal window
     * @param isEngOnly
     */
    public void setRuSpeechInModal(boolean isEngOnly)
    {
        if (isEngOnly)
        {
            context.getSharedPreferences(KEY_ENG_ONLY_MODAL, MODE_PRIVATE).edit().putBoolean(KEY_ENG_ONLY_MODAL, true).apply();
        } else
        {
            context.getSharedPreferences(KEY_ENG_ONLY_MODAL, MODE_PRIVATE).edit().putBoolean(KEY_ENG_ONLY_MODAL, false).apply();
        }
    }

    /**
     *
     * @return true if set english and russian speech, otherwise false
     */
    public boolean isRuSpeechInModal()
    {
        return context.getSharedPreferences(KEY_ENG_ONLY_MODAL, MODE_PRIVATE).getBoolean(KEY_ENG_ONLY_MODAL, false);
    }

    /**
     * Save the ArrayList<String>, converting it to the String
     * @param list (ArrayList)
     */
    public void savePlayList(ArrayList<String> list)
    {
        if (list != null && list.size() > 0)
        {
            String temp = ObjectSerializer.serialize(list);
            context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putString(KEY_PLAY_LIST_ITEMS, temp).apply();
            AppData appData = AppData.getInstance();

            while (appData.getNdict() > list.size()-1)
            {
                appData.setNdict(appData.getNdict()-1);
                appData.setNword(1);
            }
            if (appData.getNdict() < 0)
            {
                appData.setNdict(0);
                appData.setNword(1);
            }
        }
        else if (list != null && list.size() == 0)
        {
            AppData appData = AppData.getInstance();
            appData.setNdict(0);
            setDictNumber(0);
            appData.setNword(1);
            setWordNumber(1);
            context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putString(KEY_PLAY_LIST_ITEMS, ObjectSerializer.serialize(list)).apply();
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
            savePlayList(list);
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
            list = (ArrayList<String>) ObjectSerializer.deserialize(play_list_items);
        }
        return list;
    }

    /***
     *
     */
    public void cleanPlayList()
    {
        savePlayList(new ArrayList<String>());
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

    public void setTranslateLang(String langCode)
    {
        context.getSharedPreferences(KEY_TRANSLATE_LANG, MODE_PRIVATE).edit().putString(KEY_TRANSLATE_LANG, langCode).apply();
    }

    public String getTranslateLang()
    {
        String defaultLangCode = getTransLangList().get(0);
        return context.getSharedPreferences(KEY_TRANSLATE_LANG, MODE_PRIVATE).getString(KEY_TRANSLATE_LANG, defaultLangCode);
    }

    public ArrayList<String> getTransLangList()
    {
        return transLangList;
    }


}
