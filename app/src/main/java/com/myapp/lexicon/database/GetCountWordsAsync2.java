package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;

public class GetCountWordsAsync2 extends AsyncTask<String, Void, Integer[]>
{
    private GetCountListener listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;
    private String tableName;

    public GetCountWordsAsync2(Activity activity, String tableName, GetCountListener listener)
    {
        setTaskCompleteListener(listener);
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
        this.tableName = StringOperations.getInstance().spaceToUnderscore(tableName);
    }

    public interface GetCountListener
    {
        void onTaskComplete(Integer[] resArray);
    }

    private void setTaskCompleteListener(GetCountListener listener)
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
    protected Integer[] doInBackground(String... params)
    {
        Cursor cursor = null;
        Integer[] count = new Integer[2];
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.query(tableName, new String[]{"max(RowId)", "count(RowId)"}, "CountRepeat <> 0", null, null, null, null);
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast())
                    {
                        count[0] = cursor.getInt(0);
                        count[1] = cursor.getInt(1);
                        cursor.moveToNext();
                    }
                }
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
    protected void onPostExecute(Integer[] count)
    {
        super.onPostExecute(count);
        if (listener != null)
        {
            listener.onTaskComplete(count);
        }
        lockOrientation.unLock();
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        lockOrientation.unLock();
    }
}
