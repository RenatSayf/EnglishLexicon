package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;

import java.lang.ref.WeakReference;

/**
 * Created by Renat on 09.03.2017.
 */

public class GetWordsCountAsync extends AsyncTask<String, Void, Integer>
{
    private WeakReference<AsyncTaskListener> listener;
    private LockOrientation lockOrientation;
    private Activity activity;
    private DatabaseHelper databaseHelper;

    public GetWordsCountAsync(Activity activity, AsyncTaskListener listener)
    {
        this.activity = activity;
        lockOrientation = new LockOrientation(activity);
        setTaskCompleteListener(listener);
        databaseHelper = new DatabaseHelper(activity.getApplicationContext());
        databaseHelper.create_db();
    }

    public interface AsyncTaskListener
    {
        void onTaskComplete(int wordsCount);
    }

    private void setTaskCompleteListener(AsyncTaskListener listener)
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
    protected Integer doInBackground(String... params)
    {
        Cursor cursor = null;
        int count = 0;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor=databaseHelper.database.query(params[0], null, null, null, null, null, null);
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
        AsyncTaskListener listener = this.listener.get();
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
