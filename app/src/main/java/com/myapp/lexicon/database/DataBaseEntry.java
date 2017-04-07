package com.myapp.lexicon.database;


public class DataBaseEntry
{
    private String english;
    private String translate;
    private String countRepeat;

    public DataBaseEntry(String english, String translate)
    {
        this.english =english;
        this.translate =translate;
    }

    public DataBaseEntry(String english, String translate, String count_repeat)
    {
        this.english =english;
        this.translate =translate;
        this.countRepeat =count_repeat;
    }

    public String getEnglish()
    {
        return english;
    }

    public void setEnglish(String english)
    {
        this.english = english;
    }

    public String getTranslate()
    {
        return translate;
    }

    public void setTranslate(String translate)
    {
        this.translate = translate;
    }

    public String getCountRepeat()
    {
        return countRepeat;
    }

}
