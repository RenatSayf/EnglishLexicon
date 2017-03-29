package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Renat on 29.03.2017.
 */

public class GetTableListAsync extends AsyncTask<Void, Void, ArrayList<String>>
{
    private WeakReference<GetTableListListener> listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;

    public GetTableListAsync(Activity activity, GetTableListListener listener)
    {
        //Activity activity1 = activity;
        lockOrientation = new LockOrientation(activity);
        setListener(listener);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
    }

    public interface GetTableListListener
    {
        void getTableListListener(ArrayList<String> arrayList);
    }

    private void setListener(GetTableListListener listener)
    {
        this.listener = new WeakReference<>(listener);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        lockOrientation.lock();
    }

    @Override
    protected ArrayList<String> doInBackground(Void... params)
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

    @Override
    protected void onPostExecute(ArrayList<String> list)
    {
        super.onPostExecute(list);
        GetTableListListener listener = this.listener.get();
        if (listener != null)
        {
            listener.getTableListListener(list);
        }
        lockOrientation.unLock();
    }
}
