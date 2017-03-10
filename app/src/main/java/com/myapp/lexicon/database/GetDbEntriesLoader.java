package com.myapp.lexicon.database;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.myapp.lexicon.DataBaseEntry;

import java.util.ArrayList;

/**
 * Created by Renat on 10.03.2017.
 */

public class GetDbEntriesLoader extends AsyncTaskLoader<ArrayList<DataBaseEntry>>
{
    public static final String KEY_TABLE_NAME = "key_table_name";
    public static final String KEY_START_ID = "key_start_id";
    public static final String KEY_END_ID = "key_end_id";

    private String command;
    private String tableName;
    private int startId, endId;

    private DatabaseHelper databaseHelper;

    public GetDbEntriesLoader(Context context, Bundle bundle)
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
    }

    @Override
    public ArrayList<DataBaseEntry> loadInBackground()
    {
        return getDbEntries(tableName, startId, endId);
    }

    private ArrayList<DataBaseEntry> getDbEntries(String tableName, int startId, int endId)
    {
        ArrayList<DataBaseEntry> entries = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT max(RowId) FROM " + tableName, null);
                cursor.moveToFirst();
                int maxRowId = (int)cursor.getLong(0);

                if (startId <= maxRowId && endId > maxRowId)
                {
                    endId = maxRowId;
                }
                if (startId > maxRowId)
                {
                    startId = maxRowId;
                    endId = startId;
                }
                cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId, null);
                if (cursor != null && cursor.getCount() > 0)
                {
                    if (cursor.moveToFirst())
                    {
                        while ( !cursor.isAfterLast() )
                        {
                            DataBaseEntry dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1), String.valueOf(maxRowId));
                            entries.add(dataBaseEntry);
                            cursor.moveToNext();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }

        return entries;
    }
}
