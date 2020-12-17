package com.myapp.lexicon.database;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;

public class UpdateDBEntryAsync extends AsyncTask<Void, Void, Integer>
{
    private final LockOrientation lockOrientation;
    private final DatabaseHelper databaseHelper;
    private final ContentValues values;
    private final String table_name;
    private final String where;
    private final String[] whereArgs;
    private final IUpdateDBListener iUpdateDBListener;

    public UpdateDBEntryAsync(Activity activity, String tableName, ContentValues values, String where, String[] whereArgs, IUpdateDBListener listener)
    {
        iUpdateDBListener = listener;
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
        this.table_name =  StringOperations.getInstance().spaceToUnderscore(tableName);
        this.values = values;
        this.where = where;
        this.whereArgs = whereArgs;
    }

    public interface IUpdateDBListener
    {
        void updateDBEntry_OnComplete(int rows);
    }

    @Override
    protected void onPreExecute()
    {
        lockOrientation.lock();
    }

    @Override
    protected Integer doInBackground(Void... voids)
    {
        int rows = -1;
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                databaseHelper.database.beginTransaction();
                rows = databaseHelper.database.update(table_name, values, where, whereArgs);
                databaseHelper.database.setTransactionSuccessful();
                databaseHelper.database.endTransaction();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            databaseHelper.close();
        }

        return rows;
    }

    @Override
    protected void onPostExecute(Integer integer)
    {
        super.onPostExecute(integer);
        if (iUpdateDBListener != null)
        {
            iUpdateDBListener.updateDBEntry_OnComplete(integer);
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
