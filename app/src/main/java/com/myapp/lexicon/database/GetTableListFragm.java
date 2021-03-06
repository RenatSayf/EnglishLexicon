package com.myapp.lexicon.database;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import com.myapp.lexicon.helpers.StringOperations;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 *
 */
public class GetTableListFragm extends Fragment
{
    public static final String TAG = "get_table_list_frag_tag";
    private static OnTableListListener mListener;

    public GetTableListFragm()
    {
        // Required empty public constructor
    }

    //@SuppressLint("StaticFieldLeak")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        performAsyncOperation(getActivity());
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        if (context instanceof OnTableListListener)
        {
            mListener = (OnTableListListener) context;
        } else
        {
            throw new RuntimeException(context.toString() + " must implement OnGetEntriesFinishListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

     public interface OnTableListListener
    {
        void onGetTableListListener(Object object);
    }

    private static void performAsyncOperation(final Activity activity)
    {
        AsyncTask<Void, Void, ArrayList<String>> asyncTask = new AsyncTask<Void, Void, ArrayList<String>>()
        {
            @Override
            protected ArrayList<String> doInBackground(Void... params)
            {
                String nameNotDict;
                Cursor cursor = null;
                ArrayList<String> list = new ArrayList<>();
                try
                {
                    DatabaseHelper databaseHelper = new DatabaseHelper(activity);
                    databaseHelper.create_db();
                    databaseHelper.open();
                    if (databaseHelper.database.isOpen())
                    {
                        cursor = databaseHelper.database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", null);
                    }

                    if (cursor != null && cursor.getCount() > 0)
                    {
                        if (cursor.moveToFirst())
                        {
                            while (!cursor.isAfterLast())
                            {
                                nameNotDict = cursor.getString(cursor.getColumnIndex("name"));
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
                } catch (Exception e)
                {
                    e.printStackTrace();
                } finally
                {
                    if (cursor != null)
                    {
                        cursor.close();
                    }
                }
                return list;
            }

            @Override
            protected void onPostExecute(ArrayList<String> arrayList)
            {
                super.onPostExecute(arrayList);
                if (mListener != null)
                {
                    mListener.onGetTableListListener(arrayList);
                }
            }
        };
        if (asyncTask.getStatus() != AsyncTask.Status.RUNNING)
        {
            asyncTask.execute();
        }
    }
}
