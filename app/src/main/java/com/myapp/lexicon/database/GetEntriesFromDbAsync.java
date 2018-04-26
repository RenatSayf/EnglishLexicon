package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;

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
    private String cmd;

    public GetEntriesFromDbAsync(Activity activity, String tableName, int startId, int endId, GetEntriesListener listener)
    {
        setListener(listener);
        this.activity = activity;
        lockOrientation = new LockOrientation(this.activity);
        databaseHelper = new DatabaseHelper(this.activity);
        databaseHelper.create_db();
        tableName =  StringOperations.getInstance().spaceToUnderscore(tableName);
        this.cmd = "SELECT * FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId;
    }

    public GetEntriesFromDbAsync(Activity activity, String tableName, int[] rowId, GetEntriesListener listener)
    {
        setListener(listener);
        this.activity = activity;
        lockOrientation = new LockOrientation(this.activity);
        databaseHelper = new DatabaseHelper(this.activity);
        databaseHelper.create_db();
        tableName = StringOperations.getInstance().spaceToUnderscore(tableName);
        String idSequence = "";
        for (int i = 0; i < rowId.length; i++)
        {
            int item = rowId[i];
            if (i != rowId.length - 1)
            {
                idSequence = idSequence.concat(item + ",");
            }
            if (i == rowId.length - 1)
            {
                idSequence = idSequence.concat(item + "");
            }
        }
        String orderBy = "";
        if (rowId.length > 1)
        {
            if (rowId[1] > rowId[0])
            {
                orderBy = "ASC";
            }
            else if (rowId[0] > rowId[1])
            {
                orderBy = "DESC";
            }
        }
        this.cmd = "SELECT * FROM " + tableName + " WHERE RowID IN(" + idSequence + ") ORDER BY RowId " + orderBy + ";";
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
            activity = null;
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
                cursor = databaseHelper.database.rawQuery(cmd, null);
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
            activity = null;
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
            activity = null;
        }
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        if (activity != null)
        {
            lockOrientation.unLock();
            activity = null;
        }
    }
}
