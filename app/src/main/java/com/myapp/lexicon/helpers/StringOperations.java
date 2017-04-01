package com.myapp.lexicon.helpers;

/**
 * Performs various operations on strings
 */
public class StringOperations
{
    private static StringOperations ourInstance = new StringOperations();

    public static StringOperations getInstance()
    {
        return ourInstance;
    }

    private StringOperations()
    {
    }
    public String[] getLangOfText(String text)
    {
        String[] lang = new String[2];
        int char_first;
        for (int i = 0; i < text.length(); i++)
        {
            char_first = text.codePointAt(i);
            if ((char_first >= 33 && char_first <= 64) || (text.codePointAt(i) >= 91 && text.codePointAt(i) <= 96) || (text.codePointAt(i) >= 123 && text.codePointAt(i) <= 126))
            {
                continue;
            }
            if (text.codePointAt(i) >= 1025 && text.codePointAt(i) <= 1105)
            {
                lang[0] = "ru-en";
                lang[1] = "ru";
            }
            else if (text.codePointAt(i) >= 65 && text.codePointAt(i) <= 122)
            {
                lang[0] = "en-ru";
                lang[1] = "en";
            }
            else
            {
                lang[0] = null;
                lang[1] = null;
            }
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
