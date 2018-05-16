package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;

public class GetStudiedWordsCount extends AsyncTask<String, Void, Integer[]>
{
    private GetCountListener listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;
    private String tableName;

    public GetStudiedWordsCount(Activity activity, String tableName, GetCountListener listener)
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
        Integer[] countArray = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {

                String cmd = "SELECT count(RowId) FROM " + tableName + " WHERE (CountRepeat <> 0) UNION ALL SELECT count(rowId) FROM " + tableName;
                cursor = databaseHelper.database.rawQuery(cmd, null);
                if (cursor.moveToFirst())
                {
                    countArray = new Integer[cursor.getCount()];
                    int i = 0;
                    while (!cursor.isAfterLast())
                    {
                        countArray[i] = cursor.getInt(0);
                        cursor.moveToNext();
                        i++;
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
        return countArray;
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
