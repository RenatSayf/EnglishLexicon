package com.myapp.lexicon;


import java.util.HashMap;

public class Word extends HashMap<String,String>
{
    public static final String ENGLISH = "english";
    public static final String TRANSLATE = "translate";

    public Word(String english, String translate)
    {
        super();
        super.put(ENGLISH, english);
        super.put(TRANSLATE, translate);
    }
}
