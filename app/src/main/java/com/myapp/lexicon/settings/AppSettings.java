package com.myapp.lexicon.settings;

import android.content.Context;

import com.myapp.lexicon.helpers.ObjectSerializer;
import com.myapp.lexicon.models.Word;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;

/**
 *
 * Helper class for work with the SharedPreferences
 */

public class AppSettings
{

    private final String KEY_IS_SOUND = this.getClass().getCanonicalName() + ".KEY_IS_SOUND";
    private final String KEY_ENG_SPEECH = this.getClass().getCanonicalName() + ".KEY_ENG_SPEECH";
    private final String KEY_RUS_SPEECH = this.getClass().getCanonicalName() + ".KEY_RUS_SPEECH";
    private final String KEY_PLAY_LIST = this.getClass().getCanonicalName() + ".KEY_PLAY_LIST";
    private final String KEY_PLAY_LIST_ITEMS = this.getClass().getCanonicalName() + ".KEY_PLAY_LIST_ITEMS";
    private final String KEY_CURRENT_DICT = this.getClass().getCanonicalName() + ".KEY_CURRENT_DICT";
    private final String KEY_WORD_IDS = this.getClass().getCanonicalName() + ".KEY_WORD_IDS";

    private final Context context;

    @Inject
    public AppSettings(Context context)
    {
        this.context = context;
    }

    public boolean isSpeech()
    {
        return context.getSharedPreferences(KEY_IS_SOUND, MODE_PRIVATE).getBoolean(KEY_IS_SOUND, false);
    }

    public void enableSpeech(Boolean isEnable)
    {
        context.getSharedPreferences(KEY_IS_SOUND, MODE_PRIVATE).edit().putBoolean(KEY_IS_SOUND, isEnable).apply();
    }

    /**
     *  Set the setting of english speech synthesis only, either no
     * @param isSpeech    true - set only english speech or false - set english and default speech
     */
    public void setEngSpeech(boolean isSpeech)
    {
        context.getSharedPreferences(KEY_ENG_SPEECH, MODE_PRIVATE).edit().putBoolean(KEY_ENG_SPEECH, isSpeech).apply();
    }

    /**
     *
     * @return  true if set english speech, otherwise false
     */
    public boolean isEngSpeech()
    {
        return context.getSharedPreferences(KEY_ENG_SPEECH, MODE_PRIVATE).getBoolean(KEY_ENG_SPEECH, true);
    }

    /**
     *  Set the setting of default speech synthesis, either no
     * @param isSpeech    true - enable default speech or false - disable default speech
     */
    public void setRusSpeech(boolean isSpeech)
    {
        context.getSharedPreferences(KEY_RUS_SPEECH, MODE_PRIVATE).edit().putBoolean(KEY_RUS_SPEECH, isSpeech).apply();
    }

    /**
     *
     * @return  true if set default speech, otherwise false
     */
    public boolean isRusSpeech()
    {
        return context.getSharedPreferences(KEY_RUS_SPEECH, MODE_PRIVATE).getBoolean(KEY_RUS_SPEECH, false);
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
            //noinspection unchecked
            list = (ArrayList<String>) ObjectSerializer.deserialize(play_list_items);
        }
        return list;
    }

    public void goForward(List<Word> words) //????
    {
        if (words != null)
        {
            if (words.size() > 1)
            {
                saveWordThePref(words.get(1));
            }
            if (words.size() == 1)
            {
                Word word = new Word(1, words.get(0).getDictName(), "", "", 1);
                saveWordThePref(word);
            }
            if (words.isEmpty())
            {
                Word word = new Word(1, "default", "", "", 1);
                saveWordThePref(word);
            }
        }
    }

    private final String WORD_ID = this.getClass().getCanonicalName() + ".WORD_ID";

    public void saveWordThePref(Word word)
    {
        context.getSharedPreferences(WORD_ID, MODE_PRIVATE).edit().putString(KEY_CURRENT_DICT, word.getDictName()).apply();
        context.getSharedPreferences(WORD_ID, MODE_PRIVATE).edit().putInt(WORD_ID, word.get_id()).apply();
    }

    public void saveWordsIdAsString(String strIds)
    {
        context.getSharedPreferences(WORD_ID, MODE_PRIVATE).edit().putString(KEY_WORD_IDS, strIds).apply();
    }

    public String getWordsIdsAsString()
    {
        return context.getSharedPreferences(WORD_ID, MODE_PRIVATE).getString(KEY_WORD_IDS, "");
    }

}
