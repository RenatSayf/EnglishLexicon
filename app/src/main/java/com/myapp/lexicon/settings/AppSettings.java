package com.myapp.lexicon.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.Word;
import com.myapp.lexicon.helpers.ObjectSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.preference.PreferenceManager;

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
    private final String KEY_ORDER_PLAY = this.getClass().getCanonicalName() + ".KEY_ORDER_PLAY";
    private final String KEY_N_WORD = this.getClass().getCanonicalName() + ".KEY_N_WORD";
    private final String KEY_ROW_ID = this.getClass().getCanonicalName() + ".KEY_ROW_ID";
    private final String KEY_CURRENT_DICT = this.getClass().getCanonicalName() + ".KEY_CURRENT_DICT";
    private final String KEY_CURRENT_WORD = this.getClass().getCanonicalName() + ".KEY_CURRENT_WORD";
    private final String KEY_EN_WORD = this.getClass().getCanonicalName() + ".KEY_EN_WORD";
    private final String KEY_RU_WORD = this.getClass().getCanonicalName() + ".KEY_RU_WORD";
    private final String KEY_IS_PAUSE = this.getClass().getCanonicalName() + ".KEY_IS_PAUSE";
    private final String KEY_TRANSLATE_LANG = this.getClass().getCanonicalName() + ".KEY_TRANSLATE_LANG";
    private final String KEY_COUNT_REPEAT = this.getClass().getCanonicalName() + ".KEY_COUNT_REPEAT";
    private final String KEY_WORD_IDS = this.getClass().getCanonicalName() + ".KEY_WORD_IDS";

    private final Context context;
    private final String transLang;

    @Inject
    public AppSettings(Context context)
    {
        this.context = context;
        transLang = context.getString(R.string.lang_code_translate);
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

    public void saveCurrentWord(DataBaseEntry entry)
    {
        SharedPreferences preferences = context.getSharedPreferences(KEY_CURRENT_WORD, MODE_PRIVATE);
        preferences.edit().putInt(KEY_ROW_ID, entry.getRowId()).apply();
        preferences.edit().putString(KEY_CURRENT_DICT, entry.getDictName()).apply();
        preferences.edit().putString(KEY_EN_WORD, entry.getEnglish()).apply();
        preferences.edit().putString(KEY_RU_WORD, entry.getTranslate()).apply();
        preferences.edit().putString(KEY_COUNT_REPEAT, entry.getCountRepeat()).apply();
    }

    public DataBaseEntry getCurrentWord()
    {
        SharedPreferences preferences = context.getSharedPreferences(KEY_CURRENT_WORD, MODE_PRIVATE);
        int rowId = preferences.getInt(KEY_ROW_ID, 0);
        String defaultDict = "";
        if (!getPlayList().isEmpty())
        {
            defaultDict = getPlayList().get(0);
        }
        String dictName = preferences.getString(KEY_CURRENT_DICT, defaultDict);
        String english = preferences.getString(KEY_EN_WORD, "");
        String translate = preferences.getString(KEY_RU_WORD, "");
        String countRepeat = preferences.getString(KEY_COUNT_REPEAT, "");
        return new DataBaseEntry(rowId, dictName, english, translate, countRepeat);
    }

    public void set_N_Word(int number)
    {
        context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).edit().putInt(KEY_N_WORD, number).apply();
    }

    public int getWordNumber()
    {
        int wordNumber = context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).getInt(KEY_N_WORD, 1);
        if (wordNumber <= 0)
        {
            wordNumber = 1;
        }
        return wordNumber;
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


    public void setTranslateLang(String langCode)
    {
        context.getSharedPreferences(KEY_TRANSLATE_LANG, MODE_PRIVATE).edit().putString(KEY_TRANSLATE_LANG, langCode).apply();
    }

    public String getTranslateLang()
    {
        String defaultLangCode = getTransLang();
        return context.getSharedPreferences(KEY_TRANSLATE_LANG, MODE_PRIVATE).getString(KEY_TRANSLATE_LANG, defaultLangCode);
    }

    public String getTransLang()
    {
        return transLang;
    }

    public void goForward(List<Word> words)
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

    public int getWordsInterval()
    {
        String wordsInterval = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_test_interval), "10");
        if (wordsInterval != null)
        {
            return Integer.parseInt(wordsInterval);
        }
        return Integer.MAX_VALUE;
    }

    private final String WORD_ID = this.getClass().getCanonicalName() + ".WORD_ID";
    public Word getWordFromPref()
    {
        String defaultText = context.getString(R.string.nav_play_list);
        String dict = Objects.requireNonNull(context.getSharedPreferences(WORD_ID, MODE_PRIVATE).getString(KEY_CURRENT_DICT, defaultText));
        int id = context.getSharedPreferences(WORD_ID, MODE_PRIVATE).getInt(WORD_ID, 1);
        return new Word(id, dict, "", "", 1);
    }

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
