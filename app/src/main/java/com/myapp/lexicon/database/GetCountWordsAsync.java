package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;

/**
 * Get the number of records from table of database
 */

public class GetCountWordsAsync extends AsyncTask<String, Void, Integer>
{
    private Activity activity;
    private GetCountListener listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;
    private String tableName;

    public GetCountWordsAsync(Activity activity, String tableName, GetCountListener listener)
    {
        setTaskCompleteListener(listener);
        this.activity = activity;
        lockOrientation = new LockOrientation(this.activity);
        databaseHelper = new DatabaseHelper(this.activity);
        databaseHelper.create_db();
        this.tableName = StringOperations.getInstance().spaceToUnderscore(tableName);
    }

    public interface GetCountListener
    {
        void onTaskComplete(int count);
    }

    private void setTaskCompleteListener(GetCountListener listener)
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
    protected Integer doInBackground(String... params)
    {
        Cursor cursor = null;
        int count = 0;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor=databaseHelper.database.query(tableName, null, null, null, null, null, null);
                count = cursor.getCount();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            lockOrientation.unLock();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
        }
        return count;
    }

    @Override
    protected void onPostExecute(Integer count)
    {
        super.onPostExecute(count);
        if (listener != null)
        {
            listener.onTaskComplete(count);
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
