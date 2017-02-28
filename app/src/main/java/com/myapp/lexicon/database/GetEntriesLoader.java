package com.myapp.lexicon.database;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

/**
 * Created by Renat on 28.02.2017.
 */

public class GetEntriesLoader extends AsyncTaskLoader<Cursor>
{
    public static final String KEY_TABLE_NAME = "key_table_name";
    public static final String KEY_START_ID = "key_start_id";
    public static final String KEY_END_ID = "key_end_id";

    private String command;
    private String tableName;
    private int startId, endId;

    private DatabaseHelper databaseHelper;

    public GetEntriesLoader(Context context, Bundle bundle)
    {
        super(context);
        databaseHelper = new DatabaseHelper(context);
        databaseHelper.create_db();
        if (bundle != null)
        {
            tableName = bundle.getString(KEY_TABLE_NAME);
            startId = bundle.getInt(KEY_START_ID);
            endId = bundle.getInt(KEY_END_ID);
        }
        return;
    }

    @Override
    public Cursor loadInBackground()
    {
        return getEntriesFromDB(tableName, startId, endId);
    }

    private Cursor getEntriesFromDB(String tableName, int startId, int endId)
    {
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId, null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return cursor;
    }




}
