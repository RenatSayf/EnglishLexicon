package com.myapp.lexicon.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.myapp.lexicon.R;

import java.util.ArrayList;

/**
 * Created by Renat on 06.03.2018.
 */

public class LangListViewAdapter extends BaseAdapter
{
    private ArrayList<String> langs;
    private Context context;

    public LangListViewAdapter(ArrayList<String> langs, Context context)
    {
        this.langs = langs;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return langs.size();
    }

    @Override
    public Object getItem(int i)
    {
        return langs.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        View langView = view;
        if (langView == null)
        {
            langView = LayoutInflater.from(context).inflate(R.layout.a_lang_layout, null);
        }
        return langView;
    }
}
