package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;

import java.util.ArrayList;

/**
 * Created by Renat on 10.03.2017.
 */

public class GetEntriesAsync extends AsyncTask<String, Void, Cursor>
{
    private AsyncTaskListener listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;

    public GetEntriesAsync(Activity activity, AsyncTaskListener listener)
    {
        lockOrientation = new LockOrientation(activity);
        setTaskCompleteListener(listener);
        databaseHelper = new DatabaseHelper(activity.getApplicationContext());
        databaseHelper.create_db();
    }

    public interface AsyncTaskListener
    {
        void onTaskComplete(ArrayList<DataBaseEntry> entries);
    }

    private void setTaskCompleteListener(AsyncTaskListener listener)
    {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        lockOrientation.lock();
    }

    @Override
    protected Cursor doInBackground(String... params)
    {
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                String table_name = StringOperations.getInstance().spaceToUnderscore(params[0]);
                cursor = databaseHelper.database.rawQuery("SELECT max(RowId) FROM " + table_name, null);
                cursor.moveToFirst();
                cursor = databaseHelper.database.rawQuery("SELECT * FROM " + table_name + " WHERE RowID BETWEEN " + params[1] +" AND " + params[2], null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return cursor;
    }

    @Override
    protected void onPostExecute(Cursor cursor)
    {
        super.onPostExecute(cursor);
        if (listener != null)
        {
            ArrayList<DataBaseEntry> entries = new ArrayList<>();
            try
            {
                if (cursor != null && cursor.getCount() == 1)
                {
                    if (cursor.moveToFirst())
                    {
                        while ( !cursor.isAfterLast() )
                        {
                            DataBaseEntry dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1), cursor.getString(3));
                            entries.add(dataBaseEntry);
                            cursor.moveToNext();
                        }
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                lockOrientation.unLock();
            } finally
            {
                if (cursor != null)
                {
                    cursor.close();
                }
                listener.onTaskComplete(entries);
                lockOrientation.unLock();
            }
        }
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        lockOrientation.unLock();
    }
}
