package com.myapp.lexicon.database;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;

import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.StringOperations;

import java.util.ArrayList;

/**
 * Get list of all table from database asynchronously
 */

public class GetTableListAsync extends AsyncTask<Void, Void, ArrayList<String>>
{
    private GetTableListListener listener;
    private LockOrientation lockOrientation;
    private DatabaseHelper databaseHelper;

    public GetTableListAsync(Activity activity, GetTableListListener listener)
    {
        setListener(listener);
        lockOrientation = new LockOrientation(activity);
        databaseHelper = new DatabaseHelper(activity);
        databaseHelper.create_db();
    }

    public interface GetTableListListener
    {
        void getTableListListener(ArrayList<String> arrayList);
    }

    private void setListener(GetTableListListener listener)
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
    protected ArrayList<String> doInBackground(Void... params)
    {
        String nameNotDict;
        Cursor cursor = null;
        ArrayList<String> list = new ArrayList<>();
        try
        {
            databaseHelper.open();
            if (databaseHelper.database.isOpen())
            {
                cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null);
            }

            if (cursor != null && cursor.getCount() > 0)
            {
                if (cursor.moveToFirst())
                {
                    while ( !cursor.isAfterLast() )
                    {
                        nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                        if (!nameNotDict.equals(DatabaseHelper.TABLE_METADATA) && !nameNotDict.equals(DatabaseHelper.TABLE_SEQUENCE) && !nameNotDict.equals(DatabaseHelper.TABLE_API_KEY))
                        {
                            String table_name = cursor.getString(cursor.getColumnIndex("name"));
                            table_name = StringOperations.getInstance().underscoreToSpace(table_name);
                            list.add(table_name);
                        }
                        cursor.moveToNext();
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            databaseHelper.close();
        }
        finally
        {
            if (cursor != null)
            {
                cursor.close();
            }
            databaseHelper.close();
        }
        return list;
    }

    @Override
    protected void onPostExecute(ArrayList<String> list)
    {
        super.onPostExecute(list);
        if (listener != null)
        {
            listener.getTableListListener(list);
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
