package com.myapp.lexicon.database;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Created by Renat on 20.03.2017.
 */

public class LoaderHandler
{
    private Context context;

    public LoaderHandler(Context context)
    {
        this.context = context;
    }

    public ArrayList<String> getTableArrayList(Cursor cursor)
    {
        String nameNotDict;
        ArrayList<String> list = new ArrayList<>();
        try
        {
            if (cursor != null && cursor.getCount() > 0)
            {
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
