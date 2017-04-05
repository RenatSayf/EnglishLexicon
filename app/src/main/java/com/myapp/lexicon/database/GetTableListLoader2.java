package com.myapp.lexicon.database;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

import com.myapp.lexicon.helpers.StringOperations;

import java.util.ArrayList;

/**
 * Return table list from database as object
 */

public class GetTableListLoader2 extends AsyncTaskLoader
{
    private DatabaseHelper databaseHelper;

    public GetTableListLoader2(Context context)
    {
        super(context);
        databaseHelper = new DatabaseHelper(context);
        databaseHelper.create_db();
    }

    @Override
    public Object loadInBackground()
    {
        return GetTableListFromDB();
    }

    private Object GetTableListFromDB()
    {
        String nameNotDict;
        Cursor cursor = null;
        ArrayList<String> list = new ArrayList<>();
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            }

            if (cursor != null && cursor.getCount() > 0)
            {
                if (cursor.moveToFirst())
                {
                    while ( !cursor.isAfterLast() )
                    {
                        nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                        if (!nameNotDict.equals(DatabaseHelper.COLUMN_METADATA) && !nameNotDict.equals(DatabaseHelper.COLUMN_SEQUENCE) && !nameNotDict.equals(DatabaseHelper.COLUMN_API_KEY))
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
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return list;
    }
}
