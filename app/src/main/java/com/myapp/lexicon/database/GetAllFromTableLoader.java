package com.myapp.lexicon.database;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

/**
 * Created by Renat on 01.03.2017.
 */

public class GetAllFromTableLoader extends AsyncTaskLoader<Cursor>
{
    public static final String KEY_TABLE_NAME = "key_table_name";

    private DatabaseHelper databaseHelper;
    private String tableName;

    public GetAllFromTableLoader(Context context, Bundle bundle)
    {
        super(context);
        databaseHelper = new DatabaseHelper(context);
        databaseHelper.create_db();
        if (bundle != null)
        {
            tableName = bundle.getString(KEY_TABLE_NAME);
        }
    }

    @Override
    public Cursor loadInBackground()
    {
        return getAllFromDBTable(tableName);
    }

    private Cursor getAllFromDBTable(String tableName)
    {
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName, null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return cursor;
    }
}
