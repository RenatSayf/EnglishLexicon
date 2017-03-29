package com.myapp.lexicon.database;


import android.widget.BaseAdapter;

import java.util.HashMap;

public class DataBaseEntry
{
    private String _id;
    private String _english;
    private String _translate;
    private String _image;
    private String _count_repeat;

    public DataBaseEntry(String english, String translate, String image, String count_repeat)
    {
        this._english=english;
        this._translate=translate;
        this._image=image;
        this._count_repeat=count_repeat;
    }

    public DataBaseEntry(String english, String translate)
    {
        this._english=english;
        this._translate=translate;
    }

    public DataBaseEntry(String english, String translate, String count_repeat)
    {
        this._english=english;
        this._translate=translate;
        this._count_repeat=count_repeat;
    }

    public String get_id()
    {
        return _id;
    }

    public void set_id(String _id)
    {
        this._id = _id;
    }

    public String get_english()
    {
        return _english;
    }

    public void set_english(String _english)
    {
        this._english = _english;
    }

    public String get_translate()
    {
        return _translate;
    }

    public void set_translate(String _translate)
    {
        this._translate = _translate;
    }

    public String get_image()
    {
        return _image;
    }

    public void set_image(String _image)
    {
        this._image = _image;
    }

    public String get_count_repeat()
    {
        return _count_repeat;
    }

    public void set_count_repeat(String _count_repeat)
    {
        this._count_repeat = _count_repeat;
    }
}
