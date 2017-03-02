package com.myapp.lexicon;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.myapp.lexicon.database.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by Ренат on 01.04.2016.
 */
public class z_GetListTableFromDbAsync extends AsyncTask<Void, Void, String[]>
{
    private String[]_items;
    private DatabaseHelper _databaseHelper;
    public z_GetListTableFromDbAsync()
    {
        super();
        Log.i("Lexicon", "Вход в z_GetListTableFromDbAsync() конструктор");

    }
    public z_GetListTableFromDbAsync(Context context)
    {
        super();
        Log.i("Lexicon", "Вход в z_GetListTableFromDbAsync() конструктор");
        if (_databaseHelper == null)
        {
            _databaseHelper = new DatabaseHelper(context);
            _databaseHelper.create_db();
        }
    }
    @Override
    protected String[] doInBackground(Void... params)
    {
        Log.i("Lexicon", "Вход в z_GetListTableFromDbAsync().doInBackground()");
        String[]items=getListTableFromDb();
        Log.i("Lexicon", "Выход из z_GetListTableFromDbAsync().doInBackground() items = " + items);
        return items;
    }

    @Override
    protected void onPostExecute(String[] strings)
    {
        super.onPostExecute(strings);
        Log.i("Lexicon", "Вход в z_GetListTableFromDbAsync().onPostExecute()");
        _items=strings;
        Log.i("Lexicon", "Выход из z_GetListTableFromDbAsync().onPostExecute() items = " + _items);

    }
    private String[] getListTableFromDb()
    {
        ArrayList<String> list=new ArrayList<>();

        String[] listTable;
        String nameNotDict;
        try
        {
            _databaseHelper.open();
            SQLiteDatabase database1 = _databaseHelper.database;
            Cursor cursor = database1.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (cursor.moveToFirst())
            {
                while ( !cursor.isAfterLast() )
                {
                    nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                    if (!nameNotDict.equals("android_metadata") && !nameNotDict.equals("sqlite_sequence"))
                    {
                        list.add( cursor.getString( cursor.getColumnIndex("name")) );
                    }
                    cursor.moveToNext();
                }
            }
            listTable=new String[list.size()];
            if (list.size() > 0)
            {
                int i=0;
                for (String item : list)
                {
                    listTable[i]=item;
                    i++;
                }
            }
        } catch (Exception e)
        {
            Log.i("Lexicon", "Исключение в z_GetListTableFromDbAsync.getListTableFromDb() = " + e);
            listTable = new String[0];
        }
        finally
        {
            _databaseHelper.close();
        }
        return listTable;
    }
}
