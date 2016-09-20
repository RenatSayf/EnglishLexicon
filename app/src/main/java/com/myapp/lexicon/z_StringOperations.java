package com.myapp.lexicon;

/**
 * Created by Ренат on 11.07.2016.
 */
public class z_StringOperations
{
    private static z_StringOperations ourInstance = new z_StringOperations();

    public static z_StringOperations getInstance()
    {
        return ourInstance;
    }

    private z_StringOperations()
    {
    }
    public String[] getLangOfText(String text)
    {
        String str = text;
        String[] lang = new String[2];
        int char_first;
        for (int i = 0; i < str.length(); i++)
        {
            char_first = str.codePointAt(i);
            if ((char_first >= 33 && char_first <= 64) || (str.codePointAt(i) >= 91 && str.codePointAt(i) <= 96) || (str.codePointAt(i) >= 123 && str.codePointAt(i) <= 126))
            {
                continue;
            }
            if (str.codePointAt(i) >= 1025 && str.codePointAt(i) <= 1105)
            {
                lang[0] = "ru-en";
                lang[1] = "ru";
            }
            else if (str.codePointAt(i) >= 65 && str.codePointAt(i) <= 122)
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
}
