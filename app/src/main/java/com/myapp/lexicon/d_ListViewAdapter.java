package com.myapp.lexicon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ренат on 29.03.2016.
 */
public class d_ListViewAdapter extends BaseAdapter
{
    private ArrayList<DataBaseEntry> entries;
    private Context context;

    public d_ListViewAdapter(ArrayList<DataBaseEntry> entries, Context context)
    {
        this.entries = entries;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        return entries.size();
    }

    @Override
    public Object getItem(int position)
    {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        View wordView=convertView;
        if (wordView == null)
        {
            wordView= LayoutInflater.from(context).inflate(R.layout.d_layout_word, viewGroup, false);
        }

        DataBaseEntry dataBaseEntry = entries.get(position);

        TextView textEnglish = (TextView) wordView.findViewById(R.id.english);
        textEnglish.setText(dataBaseEntry.get_english());

        TextView textTranslate = (TextView) wordView.findViewById(R.id.translate);
        textTranslate.setText(dataBaseEntry.get_translate());

        return wordView;
    }
}
