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


    /**
     * @param activity Activity activity
     * @param tableName String tableName
     * @param listener GetCountListener listener
     */
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
        /**
         *
         * @param resArray
         * resArray[0] - minimum RowId Where CountRepeat != 0
         * resArray[1] - maximum RowId Where CountRepeat != 0
         * resArray[2] - studied word amount
         * resArray[3] - total word amount
         */
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

                String cmd = "SELECT min(RowId) FROM " + tableName + " WHERE (CountRepeat <> 0) UNION ALL SELECT max(RowId) FROM " + tableName + " WHERE (CountRepeat <> 0) UNION ALL SELECT count(rowId) FROM " + tableName +" WHERE CountRepeat == 0 UNION ALL SELECT count(rowId) FROM " + tableName;
                cursor = databaseHelper.database.rawQuery(cmd, null);
                if (cursor.moveToFirst())
                {
                    countArray = new Integer[cursor.getCount()];
                    int i = 0;
                    while (!cursor.isAfterLast())
                    {
                        try
                        {
                            countArray[i] = cursor.getInt(0);
                        } catch (Exception e)
                        {
                            countArray[i] = 0;
                        }
                        cursor.moveToNext();
                        i++;
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            databaseHelper.close();
            lockOrientation.unLock();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
            databaseHelper.close();
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
        databaseHelper.close();
        lockOrientation.unLock();
    }
}
