package com.myapp.lexicon;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Ренат on 08.12.2016.
 */

public abstract class db_GetPairWords extends AsyncTask<Object,Void,ArrayList<DataBaseEntry>>
{
    private static DatabaseHelper databaseHelper;
    private Context context;

    public db_GetPairWords(Context context)
    {
        this.context = context;
        databaseHelper = new DatabaseHelper(context);
        databaseHelper.create_db();
        try
        {
            databaseHelper.open();
        } catch (SQLException e)
        {
            z_Log.v(e.getMessage());
        }
    }

    public abstract void resultAsyncTask(ArrayList<DataBaseEntry> list);

    public ArrayList<DataBaseEntry> getWordsFromDB(String tableName, int firstId, int secondId)
    {
        ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
        DataBaseEntry dataBaseEntry;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                Cursor cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID = " + firstId +" OR RowID = " + secondId, null);
                int count = cursor.getCount();
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast())
                    {
                        dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1));
                        entriesFromDB.add(dataBaseEntry);
                        cursor.moveToNext();
                    }
                }
            } else
            {
                dataBaseEntry = new DataBaseEntry(null, null);
            }
        }
        catch (Exception e)
        {
            z_Log.v("Возникло исключение - "+e.getMessage());
            entriesFromDB.add(new DataBaseEntry(null,null));
        }
        finally
        {
            databaseHelper.database.close();
        }
        return entriesFromDB;
    }

    @Override
    protected ArrayList<DataBaseEntry> doInBackground(Object... params)
    {
        ArrayList<DataBaseEntry> entriesFromDB = getWordsFromDB((String) params[0], (int)params[1], (int)params[2]);
        return entriesFromDB;
    }

    @Override
    protected void onPostExecute(ArrayList<DataBaseEntry> entriesFromDB)
    {
        resultAsyncTask(entriesFromDB);
    }
}
