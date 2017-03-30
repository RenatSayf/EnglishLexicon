package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;

import java.util.ArrayList;

/**
 * Get one or more an entries from database
 */

public class GetEntriesFromDbAsync extends AsyncTask<String, Void, ArrayList<DataBaseEntry>>
{
    private Activity activity;
    private GetEntriesListener listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;
    private String tableName;
    private int startId;
    private int endId;

    public GetEntriesFromDbAsync(Activity activity, String tableName, int startId, int endId, GetEntriesListener listener)
    {
        setListener(listener);
        this.activity = activity;
        lockOrientation = new LockOrientation(this.activity);
        databaseHelper = new DatabaseHelper(this.activity);
        databaseHelper.create_db();
        this.tableName = tableName;
        this.startId = startId;
        this.endId = endId;
    }

    public interface GetEntriesListener
    {
        void getEntriesListener(ArrayList<DataBaseEntry> entries);
    }

    private void setListener(GetEntriesListener listener)
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
    protected ArrayList<DataBaseEntry> doInBackground(String... params)
    {
        ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
        DataBaseEntry dataBaseEntry;
        Cursor cursor = null;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId, null);
                if (cursor.moveToFirst())
                {
                    while (!cursor.isAfterLast())
                    {
                        dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1));
                        entriesFromDB.add(dataBaseEntry);
                        cursor.moveToNext();
                    }
                }
            } else
            {
                entriesFromDB.add(new DataBaseEntry(null,null));
            }
        }
        catch (Exception e)
        {
            entriesFromDB.add(new DataBaseEntry(null,null));
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
            databaseHelper.database.close();
        }
        return entriesFromDB;
    }

    @Override
    protected void onPostExecute(ArrayList<DataBaseEntry> entries)
    {
        super.onPostExecute(entries);
        if (listener != null)
        {
            listener.getEntriesListener(entries);
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
