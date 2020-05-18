package com.myapp.lexicon.database;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.myapp.lexicon.helpers.StringOperations;

/**
 * Created by Renat on 28.02.2017.
 */

public class GetEntriesLoader extends AsyncTaskLoader<Cursor>
{
    private static final String KEY_TABLE_NAME = "key_table_name";
    private static final String KEY_START_ID = "key_start_id";
    private static final String KEY_END_ID = "key_end_id";

    //private String command;
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
            tableName = StringOperations.getInstance().spaceToUnderscore(bundle.getString(KEY_TABLE_NAME));
            startId = bundle.getInt(KEY_START_ID);
            endId = bundle.getInt(KEY_END_ID);
        }
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
               cursor = databaseHelper.database.rawQuery("SELECT RowId, English, Translate, CountRepeat FROM " + tableName + " WHERE RowId >= " + startId + " And CountRepeat <> 0 ORDER BY RowId ASC LIMIT 2", null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return cursor;
    }


}
