package com.myapp.lexicon;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import static android.R.attr.entries;
import static android.R.attr.resource;

/**
 * Created by Ренат on 29.03.2016.
 */
public class d_ListViewAdapter extends ArrayAdapter implements Filterable
{
    private ArrayList<DataBaseEntry> entries;
    private Context context;

    public d_ListViewAdapter(ArrayList<DataBaseEntry> entries, Context context, int resource)
    {
        super(context, resource);
        this.entries = entries;
        this.context = context;
    }

    public d_ListViewAdapter(Context context, int resource)
    {
        super(context, resource);
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

    @NonNull
    @Override
    public Filter getFilter()
    {
        Filter filter = new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0)
                {
                    results.values = entries;
                    results.count = entries.size();
                }
                else
                {
                    ArrayList<DataBaseEntry> filteredEntries = new ArrayList<>();
                    for (DataBaseEntry entry : entries)
                    {
                        if (entry.get_english().contains(constraint) || entry.get_translate().contains(constraint))
                        {
                            filteredEntries.add(entry);
                        }
                    }
                    results.values = filteredEntries;
                    results.count = filteredEntries.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                entries = (ArrayList<DataBaseEntry>) results.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }
}
