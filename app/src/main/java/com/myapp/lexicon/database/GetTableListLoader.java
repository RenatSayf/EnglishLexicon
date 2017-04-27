package com.myapp.lexicon.database;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

/**
 * Created by Renat on 01.03.2017.
 */

public class GetTableListLoader extends AsyncTaskLoader<Cursor>
{
    private DatabaseHelper databaseHelper;

    public GetTableListLoader(Context context)
    {
        super(context);
        databaseHelper = new DatabaseHelper(context);
        databaseHelper.create_db();
    }

    @Override
    public Cursor loadInBackground()
    {
        return GetTableListFromDB();
    }

    private Cursor GetTableListFromDB()
    {
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return cursor;
    }
}
