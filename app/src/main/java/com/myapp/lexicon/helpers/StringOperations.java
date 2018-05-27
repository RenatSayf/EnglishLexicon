package com.myapp.lexicon.helpers;

import android.content.Context;

import com.myapp.lexicon.R;

/**
 * Performs various operations on strings
 */
public class StringOperations
{
    private static StringOperations ourInstance = new StringOperations();

    public static StringOperations getInstance()
    {
        if (ourInstance == null)
        {
            ourInstance = new StringOperations();
        }
        return ourInstance;
    }

    private StringOperations()
    {
    }
    public String[] getLangOfText(Context context, String text)
    {
        String[] lang = new String[2];
        try
        {
            for (int i = 0; i < text.length(); i++)
            {

                int char_first = text.codePointAt(0);
                if ((char_first >= 33 && char_first <= 64) || (text.codePointAt(i) >= 91 && text.codePointAt(i) <= 96) || (text.codePointAt(i) >= 123 && text.codePointAt(i) <= 126))
                {
                    continue;
                }
                if (text.codePointAt(i) >= 1025 && text.codePointAt(i) <= 1105)
                {
                    lang[0] = context.getString(R.string.translate_direct_ru_en);
                    lang[1] = context.getString(R.string.translate_lang_ru);
                }
                else if (text.codePointAt(i) >= 65 && text.codePointAt(i) <= 122)
                {
                    lang[0] = context.getString(R.string.translate_direct_en_ru);
                    lang[1] = context.getString(R.string.translate_lang_en);
                }
                else
                {
                    lang[0] = null;
                    lang[1] = null;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return lang;
    }

    public String spaceToUnderscore(String text)
    {
        if (text != null)
        {
            String name = text.trim();
            return name.replace(' ','_');
        } else
        {
            return "";
        }
    }

    public String underscoreToSpace(String text)
    {
        if (text != null)
        {
            String name = text.trim();
            return name.replace('_',' ');
        } else
        {
            return "";
        }
    }
}
