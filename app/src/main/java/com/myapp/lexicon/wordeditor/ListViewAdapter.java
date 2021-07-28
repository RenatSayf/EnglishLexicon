package com.myapp.lexicon.wordeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.models.Word;

import java.util.ArrayList;

import androidx.annotation.NonNull;



// TODO: ListView adapter class
public class ListViewAdapter extends BaseAdapter implements Filterable
{
    private ArrayList<Word> words;
    private final ArrayList<Word> tempWords;
    private final IListViewAdapter listener;

    ListViewAdapter(ArrayList<Word> words, IListViewAdapter listener)
    {
        this.words = words;
        this.tempWords = new ArrayList<>(words);
        this.listener = listener;
    }

    public interface IListViewAdapter
    {
        void onItemClickListener(Word word);
        void onItemCheckBoxClickListener(Word word);
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

        CheckBox disableWordCheBox = wordView.findViewById(R.id.checkStudied);
        int countRepeat = word.getCountRepeat();
        disableWordCheBox.setChecked(countRepeat > 0);
        disableWordCheBox.setOnClickListener(view ->
        {
            CheckBox checkBox = (CheckBox) view;
            Word newWord;
            if (checkBox.isChecked())
            {
                words.get(position).setCountRepeat(1);
                newWord = new Word(word.get_id(), word.getDictName(), word.getEnglish(), word.getTranslate(), 1);
            }
            else
            {
                words.get(position).setCountRepeat(-1);
                newWord = new Word(word.get_id(), word.getDictName(), word.getEnglish(), word.getTranslate(), -1);
            }
            if (listener != null)
            {
                listener.onItemCheckBoxClickListener(newWord);
            }
        });

        LinearLayout itemLayout = wordView.findViewById(R.id.itemLayout);
        itemLayout.setOnClickListener(view ->
        {
            if (listener != null)
            {
                listener.onItemClickListener(word);
            }
        });

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
                    results.values = tempWords;
                    results.count = tempWords.size();
                }
                else
                {
                    ArrayList<Word> filteredEntries = new ArrayList<>();
                    for (Word entry : tempWords)
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
