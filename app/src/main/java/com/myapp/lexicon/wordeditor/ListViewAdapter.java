package com.myapp.lexicon.wordeditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseEntry;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * Created by Ренат on 29.03.2016.
 */
// TODO: ListView adapter class
public class ListViewAdapter extends ArrayAdapter implements Filterable
{
    private ArrayList<DataBaseEntry> entries;
    private ArrayList<DataBaseEntry> tempEntries;
    private Context context;

    ListViewAdapter(ArrayList<DataBaseEntry> entries, Context context, int resource)
    {
        super(context, resource);
        this.entries = entries;
        this.context = context;
        this.tempEntries = new ArrayList<>(entries);
    }

    @Override
    public int getCount()
    {
        return entries.size();
    }

    @Override
    public DataBaseEntry getItem(int position)
    {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup viewGroup)
    {
        View wordView=convertView;
        if (wordView == null)
        {
            wordView= LayoutInflater.from(context).inflate(R.layout.d_layout_word, viewGroup, false);
        }

        DataBaseEntry dataBaseEntry = entries.get(position);

        TextView textEnglish = wordView.findViewById(R.id.english);
        textEnglish.setText(dataBaseEntry.getEnglish());

        TextView textTranslate = wordView.findViewById(R.id.translate);
        textTranslate.setText(dataBaseEntry.getTranslate());

        TextView textCountRepeat = wordView.findViewById(R.id.count_repeat);
        textCountRepeat.setText(dataBaseEntry.getCountRepeat());

        return wordView;
    }

    @NonNull
    @Override
    public Filter getFilter() //// TODO: ListView Фильтрацтя
    {
        return new Filter()
        {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0)
                {
                    results.values = tempEntries;
                    results.count = tempEntries.size();
                }
                else
                {
                    ArrayList<DataBaseEntry> filteredEntries = new ArrayList<>();
                    for (DataBaseEntry entry : tempEntries)
                    {
                        if (entry.getEnglish().contains(constraint) || entry.getTranslate().contains(constraint))
                        {
                            filteredEntries.add(entry);
                        }
                    }
                    results.values = filteredEntries;
                    results.count = filteredEntries.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results)
            {
                entries = (ArrayList<DataBaseEntry>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
