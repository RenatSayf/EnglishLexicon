package com.myapp.lexicon.wordeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.Word;

import java.util.ArrayList;

import androidx.annotation.NonNull;



// TODO: ListView adapter class
public class ListViewAdapter extends BaseAdapter implements Filterable
{
    private ArrayList<Word> words;
    private final ArrayList<Word> tempEntries;

    ListViewAdapter(ArrayList<Word> words)
    {
        this.words = words;
        this.tempEntries = new ArrayList<>(words);
    }

    @Override
    public int getCount()
    {
        return words.size();
    }

    @Override
    public Word getItem(int position)
    {
        return words.get(position);
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
        View wordView = convertView;
        if (wordView == null)
        {
            wordView= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.d_layout_word, viewGroup, false);
        }

        Word word = words.get(position);

        TextView textEnglish = wordView.findViewById(R.id.english);
        textEnglish.setText(word.getEnglish());

        TextView textTranslate = wordView.findViewById(R.id.translate);
        textTranslate.setText(word.getTranslate());

        TextView textCountRepeat = wordView.findViewById(R.id.count_repeat);
        String countRepeat = word.getCountRepeat() + "";
        textCountRepeat.setText(countRepeat);

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
                    ArrayList<Word> filteredEntries = new ArrayList<>();
                    for (Word entry : tempEntries)
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
                words = (ArrayList<Word>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
