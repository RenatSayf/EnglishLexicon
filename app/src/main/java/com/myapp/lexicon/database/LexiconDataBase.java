package com.myapp.lexicon.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.database.Cursor;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.StringOperations;
import com.myapp.lexicon.settings.AppData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LexiconDataBase extends ViewModel
{
    private MutableLiveData<List<String>> dictionaries;
    private DatabaseHelper databaseHelper;

    public LiveData<List<String>> getDictList(Context context)
    {
        databaseHelper = new DatabaseHelper(context);
        if (dictionaries == null)
        {
            dictionaries = new MutableLiveData<>();
            dictionaries = loadDictList(context);
        }
        return dictionaries;
    }

    private MutableLiveData<List<String>> loadDictList(Context context)
    {
        String nameNotDict;
        Cursor cursor = null;
        ArrayList<String> list = new ArrayList<>();
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null);
            }

            if (cursor != null && cursor.getCount() > 0)
            {
                if (cursor.moveToFirst())
                {
                    while ( !cursor.isAfterLast() )
                    {
                        nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                        if (!nameNotDict.equals(DatabaseHelper.TABLE_METADATA) && !nameNotDict.equals(DatabaseHelper.TABLE_SEQUENCE) && !nameNotDict.equals(DatabaseHelper.TABLE_API_KEY))
                        {
                            String table_name = cursor.getString(cursor.getColumnIndex("name"));
                            table_name = StringOperations.getInstance().underscoreToSpace(table_name);
                            list.add(table_name);
                        }
                        cursor.moveToNext();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            databaseHelper.close();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
            databaseHelper.close();
        }
        MutableLiveData<List<String>> dicts = new MutableLiveData<>();
        String dictName = AppData.getInstance().getPlayList().get(AppData.getInstance().getNdict());
        int dictIndex = list.indexOf(dictName);
        if (dictIndex >= 0 && dictIndex < list.size())
        {
            Collections.swap(list, dictIndex, 0);
        }
        list.add(context.getString(R.string.text_new_dict));
        dicts.setValue(list);
        return dicts;
    }
}
