package com.myapp.lexicon.settings;

import android.content.Context;

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

    private final String WORD_ID = this.getClass().getCanonicalName() + ".WORD_ID";

    public void saveWordsIdAsString(String strIds)
    {
        context.getSharedPreferences(WORD_ID, MODE_PRIVATE).edit().putString(KEY_WORD_IDS, strIds).apply();
    }

    public String getWordsIdsAsString()
    {
        return context.getSharedPreferences(WORD_ID, MODE_PRIVATE).getString(KEY_WORD_IDS, "");
    }

}
