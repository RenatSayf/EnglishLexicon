package com.myapp.lexicon.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.Word;
import com.myapp.lexicon.helpers.ObjectSerializer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

/**
 *
 * Helper class for work with the SharedPreferences
 */

public class AppSettings
{
    public final String KEY_SPINN_SELECT_ITEM = this.getClass().getCanonicalName() + ".KEY_SPINN_SELECT_ITEM";
    public final String KEY_WORD_INDEX = this.getClass().getCanonicalName() + ".KEY_WORD_INDEX";
    public final String KEY_COUNTER_RIGHT_ANSWER = this.getClass().getCanonicalName() + ".KEY_COUNTER_RIGHT_ANSWER";

    private final String KEY_ENG_SPEECH = this.getClass().getCanonicalName() + ".KEY_ENG_SPEECH";
    private final String KEY_RUS_SPEECH = this.getClass().getCanonicalName() + ".KEY_RUS_SPEECH";
    private final String KEY_ENG_ONLY_MODAL = this.getClass().getCanonicalName() + ".KEY_ENG_ONLY_MODAL";
    private final String KEY_PLAY_LIST = this.getClass().getCanonicalName() + ".KEY_PLAY_LIST";
    private final String KEY_PLAY_LIST_ITEMS = this.getClass().getCanonicalName() + ".KEY_PLAY_LIST_ITEMS";
    private final String KEY_ORDER_PLAY = this.getClass().getCanonicalName() + ".KEY_ORDER_PLAY";
    private final String KEY_N_DICT = this.getClass().getCanonicalName() + ".KEY_N_DICT";
    private final String KEY_N_WORD = this.getClass().getCanonicalName() + ".KEY_N_WORD";
    private final String KEY_ROW_ID = this.getClass().getCanonicalName() + ".KEY_ROW_ID";
    private final String KEY_CURRENT_DICT = this.getClass().getCanonicalName() + ".KEY_CURRENT_DICT";
    private final String KEY_CURRENT_WORD = this.getClass().getCanonicalName() + ".KEY_CURRENT_WORD";
    private final String KEY_EN_WORD = this.getClass().getCanonicalName() + ".KEY_EN_WORD";
    private final String KEY_RU_WORD = this.getClass().getCanonicalName() + ".KEY_RU_WORD";
    private final String KEY_IS_PAUSE = this.getClass().getCanonicalName() + ".KEY_IS_PAUSE";;
    private final String KEY_TRANSLATE_LANG = this.getClass().getCanonicalName() + ".KEY_TRANSLATE_LANG";
    private final String KEY_COUNT_REPEAT = this.getClass().getCanonicalName() + ".KEY_COUNT_REPEAT";

    private final Context context;
    private final String transLang;

    @Inject
    public AppSettings(Context context)
    {
        this.context = context;
        transLang = context.getString(R.string.lang_code_translate);
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
        return context.getSharedPreferences(KEY_RUS_SPEECH, MODE_PRIVATE).getBoolean(KEY_RUS_SPEECH, true);
    }

    /**
     * set the russian speech in the modal window
     * @param isEngOnly true
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
                set_N_Dict(appData.getNdict() - 1);
                appData.setNword(1);
            }
            if (appData.getNdict() < 0)
            {
                appData.setNdict(0);
                set_N_Dict(0);
                appData.setNword(1);
            }
        }
        else if (list != null)
        {
            AppData appData = AppData.getInstance();
            appData.setNdict(0);
            set_N_Dict(0);
            appData.setNword(1);
            set_N_Word(1);
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
            //noinspection unchecked
            list = (ArrayList<String>) ObjectSerializer.deserialize(play_list_items);
        }
        return list;
    }

    public ArrayList<String> getPlayList(boolean b)
    {

        String play_list_items = context.getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE).getString(KEY_PLAY_LIST_ITEMS, null);
        if (play_list_items != null && play_list_items.length() > 0)
        {
            //noinspection unchecked
            ArrayList<String> list = (ArrayList<String>) ObjectSerializer.deserialize(play_list_items);
            return new ArrayList<>(list);
        }
        return new ArrayList<>();
    }

    /***
     *
     */
    public void cleanPlayList()
    {
        savePlayList(new ArrayList<>());
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

    public void set_N_Dict(int number)
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
        String defaultLangCode = getTransLang();
        return context.getSharedPreferences(KEY_TRANSLATE_LANG, MODE_PRIVATE).getString(KEY_TRANSLATE_LANG, defaultLangCode);
    }

    public String getTransLang()
    {
        return transLang;
    }

    public void goForward(List<DataBaseEntry> entries)
    {
        if (entries != null && !getPlayList().isEmpty())
        {
            if (entries.size() > 1)
            {
                set_N_Word(entries.get(1).getRowId());
            }
            if ((entries.size() == 1 || entries.isEmpty()))
            {
                set_N_Word(1);
                if (getDictNumber() >= 0 && getDictNumber() <= getPlayList().size() - 2)
                {
                    int nextNDict = getDictNumber() + 1;
                    set_N_Dict(nextNDict);
                    setCurrentDict(getPlayList().get(nextNDict));
                }
                else
                {
                    set_N_Dict(0);
                    setCurrentDict(getPlayList().get(0));
                }
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

    private final String WORD_ID = this.getClass().getCanonicalName() + "TRANING_ID";
    public Word getWordFromPref()
    {
        String dict = context.getSharedPreferences(WORD_ID, MODE_PRIVATE).getString(KEY_CURRENT_DICT, "Наречия");
        int id = context.getSharedPreferences(WORD_ID, MODE_PRIVATE).getInt(WORD_ID, 1);
        return new Word(id, dict, "", "", 1);
    }

    public void saveWordThePref(Word word)
    {
        context.getSharedPreferences(WORD_ID, MODE_PRIVATE).edit().putString(KEY_CURRENT_DICT, word.getDictName()).apply();
        context.getSharedPreferences(WORD_ID, MODE_PRIVATE).edit().putInt(WORD_ID, word.get_id()).apply();
    }


}
