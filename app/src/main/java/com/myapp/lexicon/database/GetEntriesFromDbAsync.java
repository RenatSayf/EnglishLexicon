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
    private GetEntriesListener listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;
    private String cmd;
    private String additionalCmd = null;

    public GetEntriesFromDbAsync(Activity activity, String tableName, int startId, int endId, GetEntriesListener listener)
    {
        setListener(listener);
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
        tableName =  StringOperations.getInstance().spaceToUnderscore(tableName);
        this.cmd = "SELECT RowId, English, Translate, CountRepeat FROM " + tableName + " WHERE RowID BETWEEN " + startId +" AND " + endId;
    }

    public GetEntriesFromDbAsync(Activity activity, String tableName, int rowId, GetEntriesListener listener)
    {
        setListener(listener);
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
        tableName = StringOperations.getInstance().spaceToUnderscore(tableName);
        this.cmd = "SELECT RowId, English, Translate, CountRepeat FROM " + tableName + " WHERE RowId >= " + rowId + " And CountRepeat <> 0 ORDER BY RowId ASC LIMIT 2";
        this.additionalCmd = "SELECT RowId, English, Translate, CountRepeat FROM " + tableName + " WHERE RowId <> " + rowId + " ORDER BY random() LIMIT 1";
    }

    public GetEntriesFromDbAsync(Activity activity, String tableName, GetEntriesListener listener)
    {
        setListener(listener);
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
        tableName = StringOperations.getInstance().spaceToUnderscore(tableName);
        this.cmd = "SELECT RowId, English, Translate, CountRepeat FROM " + tableName + " WHERE CountRepeat <> 0 ORDER BY random() LIMIT 2";
    }

    public GetEntriesFromDbAsync(Activity activity, String tableName, int rowId, int direction, boolean x, GetEntriesListener listener)
    {
        setListener(listener);
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
        tableName = StringOperations.getInstance().spaceToUnderscore(tableName);
        if (direction > 0)
        {
            this.cmd = "SELECT RowId, English, Translate, CountRepeat FROM " + tableName + " WHERE RowId >= " + rowId + " And CountRepeat <> 0 ORDER BY RowId ASC LIMIT 2";
        } else
        {
            this.cmd = "SELECT RowId, English, Translate, CountRepeat FROM " + tableName + " WHERE RowId <= " + rowId + " And CountRepeat <> 0 ORDER BY RowId DESC LIMIT 2";
        }
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
        lockOrientation.lock();
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
                        dataBaseEntry = new DataBaseEntry(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                        entriesFromDB.add(dataBaseEntry);
                        cursor.moveToNext();
                    }
                }
                if (additionalCmd != null)
                {
                    cursor = databaseHelper.database.rawQuery(additionalCmd, null);
                    if (cursor.moveToFirst())
                    {
                        while (!cursor.isAfterLast())
                        {
                            dataBaseEntry = new DataBaseEntry(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                            entriesFromDB.add(dataBaseEntry);
                            cursor.moveToNext();
                        }
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
        lockOrientation.unLock();
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        lockOrientation.unLock();
    }
}
