package com.myapp.lexicon.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;

/**
 * Created by Ренат on 05.04.2016.
 */
public class ListViewAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<String> list;
    private AppSettings appSettings;
    private IPlayListChangeListener listener;

    ListViewAdapter(ArrayList<String> list, Context context)
    {
        this.list = list;
        this.context = context;
        appSettings = new AppSettings(this.context);
        this.listener = (IPlayListChangeListener) context;
    }
    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public Object getItem(int position)
    {
        return list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        View dictView=convertView;
        if (dictView == null)
        {
            dictView= LayoutInflater.from(context).inflate(R.layout.p_listview_item, null);
        }
        final String item =  this.list.get(position);
        TextView dictName = dictView.findViewById(R.id.textView_item);
        dictName.setText(item);

        final CheckBox isSelected = dictView.findViewById(R.id.checkBox_item);
        isSelected.setChecked(true);

        dictName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                isSelected.setChecked(false);
            }
        });

        isSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (!isChecked)
                {
                    //appSettings.removeItemFromPlayList(list, position);
                    list.remove(position);
                    if (listener != null)
                    {
                        listener.onPlayListChanged(list);
                    }
                }
            }
        });
        return dictView;
    }

    public interface IPlayListChangeListener
    {
        void onPlayListChanged(ArrayList<String> list);
    }

}
