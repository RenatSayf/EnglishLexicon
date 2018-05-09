package com.myapp.lexicon.database;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.myapp.lexicon.helpers.StringOperations;

import java.util.ArrayList;

public class GetEntriesAsyncFragm extends Fragment
{
    public static final String TAG = "GetEntriesAsyncFragm";
    public static final String KEY_TABLE_NAME = "table_name";
    public static final String KEY_START_ID = "start_id";
    public static final String KEY_END_ID = "end_id";
    private static OnGetEntriesFinishListener mListener;

    public GetEntriesAsyncFragm()
    {
        // Required empty public constructor
    }

    public static GetEntriesAsyncFragm newInstance(String tableName, int startId, int endId)
    {
        GetEntriesAsyncFragm asyncFragm = new GetEntriesAsyncFragm();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TABLE_NAME, tableName);
        bundle.putInt(KEY_START_ID, startId);
        bundle.putInt(KEY_END_ID, endId);
        asyncFragm.setArguments(bundle);
        return asyncFragm;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        databaseHelper.create_db();

        if (getArguments() != null)
        {
            String tableName = StringOperations.getInstance().spaceToUnderscore(getArguments().getString(KEY_TABLE_NAME));
            int startId = getArguments().getInt(KEY_START_ID);
            int endId = getArguments().getInt(KEY_END_ID);

            getEntriesAsync(databaseHelper, tableName, startId, endId);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context != null)
        {
            mListener = (GetEntriesAsyncFragm.OnGetEntriesFinishListener) context;
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    public interface OnGetEntriesFinishListener
    {
        void onGetEntriesFinish(ArrayList<DataBaseEntry> arrayList);
    }

    private static void getEntriesAsync(final DatabaseHelper databaseHelper, final String tableName, final int startId, final int endId)
    {
        AsyncTask<Void, Void, ArrayList<DataBaseEntry>> asyncTask = new AsyncTask<Void, Void, ArrayList<DataBaseEntry>>()
        {
            @Override
            protected ArrayList<DataBaseEntry> doInBackground(Void... params)
            {
                ArrayList<DataBaseEntry> entriesFromDB = new ArrayList<>();
                DataBaseEntry dataBaseEntry;
                Cursor cursor = null;
                try
                {
                    databaseHelper.open();
                    if (databaseHelper.database.isOpen())
                    {
                        cursor = databaseHelper.database.rawQuery("SELECT * FROM " + tableName + " WHERE RowID IN (" + startId + ", " + endId + ")", null);
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
                        entriesFromDB.add(new DataBaseEntry(null, null));
                    }
                } catch (Exception e)
                {
                    entriesFromDB.add(new DataBaseEntry(null, null));
                } finally
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
            protected void onPostExecute(ArrayList<DataBaseEntry> arrayList)
            {
                super.onPostExecute(arrayList);
                if (mListener != null)
                {
                    mListener.onGetEntriesFinish(arrayList);
                }
            }
        };

        if (asyncTask.getStatus() != AsyncTask.Status.RUNNING)
        {
            asyncTask.execute();
        }
    }
}
