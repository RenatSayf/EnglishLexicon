package com.myapp.lexicon;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ренат on 05.04.2016.
 */
public class p_ListViewAdapter extends BaseAdapter
{
    private Context _context;
    private ArrayList<String> _list;
    public p_ListViewAdapter(ArrayList<String> list, Context context)
    {
        this._list = list;
        this._context = context;
    }
    @Override
    public int getCount()
    {
        return _list.size();
    }

    @Override
    public Object getItem(int position)
    {
        return _list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View dictView=convertView;
        if (dictView == null)
        {
            dictView= LayoutInflater.from(_context).inflate(R.layout.p_listview_item, null);
        }
        final String list =  _list.get(position);
        TextView dictName = (TextView) dictView.findViewById(R.id.textView_item);
        dictName.setText(list);

        final CheckBox isSelected = (CheckBox) dictView.findViewById(R.id.checkBox_item);
        isSelected.setChecked(true);
        isSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                CompoundButton btn = buttonView;
                Log.i("Lexicon", "Вход в p_ListViewAdapter.getView().onCheckedChanged " + list);
                a_MainActivity.removeItemPlayList(list);
            }
        });
        return dictView;
    }
}
