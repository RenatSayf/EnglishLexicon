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
    private ContentValues values;
    private DataBaseEntry entry;
    private String table_name;
    private IUpdateDBListener iUpdateDBListener;

    public UpdateDBEntryAsync(Activity activity, String tableName, DataBaseEntry entry, IUpdateDBListener listener)
    {
        iUpdateDBListener = listener;
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
        this.entry = entry;
        this.table_name =  StringOperations.getInstance().spaceToUnderscore(tableName);
        values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_ENGLISH, entry.getEnglish());
        values.put(DatabaseHelper.COLUMN_TRANS, entry.getTranslate());
        values.put(DatabaseHelper.COLUMN_IMAGE, "");
        values.put(DatabaseHelper.COLUMN_Count_REPEAT, entry.getCountRepeat());
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
                rows = databaseHelper.database.update(table_name, values, "English = ? AND Translate = ?", new String[]{entry.getEnglish(), entry.getTranslate()});
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
