package com.myapp.lexicon.database;

import android.app.Activity;
import android.content.ContentValues;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;

public class UpdateDBEntryAsync extends AsyncTask<Void, Void, Integer>
{
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;
    private String cmd;
    private ContentValues values;
    private String table_name;
    private int rowId = -1;

    public UpdateDBEntryAsync(Activity activity, String tableName, int rowId, DataBaseEntry entry)
    {
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
        this.table_name =  StringOperations.getInstance().spaceToUnderscore(tableName);
        this.rowId = rowId;
        values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENGLISH, entry.getEnglish());
        values.put(DatabaseHelper.COLUMN_TRANS, entry.getTranslate());
        values.put(DatabaseHelper.COLUMN_IMAGE, "");
        values.put(DatabaseHelper.COLUMN_Count_REPEAT, entry.getCountRepeat());
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
                rows = databaseHelper.database.update(table_name, values, "RowID = " + rowId, null);
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

        lockOrientation.unLock();
    }

    @Override
    protected void onCancelled()
    {
        super.onCancelled();
        lockOrientation.unLock();
    }
}
