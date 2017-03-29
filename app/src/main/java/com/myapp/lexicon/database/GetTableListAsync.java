package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Get list of all table from database asynchronously
 */

public class GetTableListAsync extends AsyncTask<Void, Void, ArrayList<String>>
{
    private Activity activity;
    private GetTableListListener listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;

    public GetTableListAsync(Activity activity, GetTableListListener listener)
    {
        setListener(listener);
        this.activity = activity;
        lockOrientation = new LockOrientation(this.activity);
        databaseHelper = new DatabaseHelper(this.activity);
        databaseHelper.create_db();
    }

    public interface GetTableListListener
    {
        void getTableListListener(ArrayList<String> arrayList);
    }

    private void setListener(GetTableListListener listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        if (activity != null)
        {
            lockOrientation.lock();
        }
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
        //GetTableListListener listener = this.listener.get();
        if (listener != null)
        {
            listener.getTableListListener(list);
        }
        if (activity != null)
        {
            lockOrientation.unLock();
        }
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        if (activity != null)
        {
            lockOrientation.unLock();
        }
    }
}
