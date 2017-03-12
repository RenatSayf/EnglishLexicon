package com.myapp.lexicon.playlist;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.database.GetTableListLoader;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;

public class PlayList extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private Spinner spinneOrderPlay;
    private ListView listViewDict;
    private ListViewAdapter lictViewAdapter;
    private ArrayList<String> playList = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private AppSettings appSettings;

    private final int LOADER_GET_TABLE_LIST = 2423144;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.p_layout_play_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appSettings = new AppSettings(PlayList.this);

        if (databaseHelper == null)
        {
            databaseHelper = new DatabaseHelper(this);
            databaseHelper.create_db();
        }

        listViewDict = (ListView) findViewById(R.id.listView_playList);
        spinneOrderPlay = (Spinner) findViewById(R.id.spinner_order_play);
        spinneOrderPlay.setSelection(appSettings.getOrderPlay());
        spinneOrderPlay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {
                    case 0:
                        appSettings.setOrderPlay(0);
                        break;
                    case 1:
                        appSettings.setOrderPlay(1);
                        break;
                    case 2:

                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        playList = appSettings.getPlayList();

        if (playList.size() > 0)
        {
            lictViewAdapter = new ListViewAdapter(playList, PlayList.this);
            listViewDict.setAdapter(lictViewAdapter);
        }

        listViewDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
        getLoaderManager().initLoader(LOADER_GET_TABLE_LIST, savedInstanceState, this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        appSettings.savePlayList(playList);
    }

    private String[] dictArray;

    public void buttonAddClick(View view)
    {
        getLoaderManager().restartLoader(LOADER_GET_TABLE_LIST, null, PlayList.this).forceLoad();
    }

    private void dialogAddDictShow()
    {
        try
        {
            boolean[] choice = new boolean[dictArray.length];
            new AlertDialog.Builder(this).setTitle(R.string.access_dict)
                    .setMultiChoiceItems(dictArray, choice, new DialogInterface.OnMultiChoiceClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked)
                        {
                            if (isChecked)
                            {
                                if (!playList.contains(dictArray[which]))
                                {
                                    playList.add(dictArray[which]);
                                }
                            }
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                lictViewAdapter = new ListViewAdapter(playList, PlayList.this);
                                listViewDict.setAdapter(lictViewAdapter);
                                appSettings.savePlayList(playList);
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).create().show();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onClickCheckBoxItem(View view)
    {
        lictViewAdapter = new ListViewAdapter(playList, PlayList.this);
        listViewDict.setAdapter(lictViewAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Loader<Cursor> loader = null;
        switch (id)
        {
            case LOADER_GET_TABLE_LIST:
                loader = new GetTableListLoader(this);
                break;
            default:
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        if (loader.getId() == LOADER_GET_TABLE_LIST)
        {
            String nameNotDict;
            ArrayList<String> list = new ArrayList<>();
            try
            {
                if (cursor != null && cursor.getCount() > 0)
                {
                    if (cursor.moveToFirst())
                    {
                        while ( !cursor.isAfterLast() )
                        {
                            nameNotDict = cursor.getString( cursor.getColumnIndex("name"));
                            if (!nameNotDict.equals("android_metadata") && !nameNotDict.equals("sqlite_sequence"))
                            {
                                list.add( cursor.getString( cursor.getColumnIndex("name")) );
                            }
                            cursor.moveToNext();
                        }
                    }
                }
                dictArray = new String[list.size()];
                for (int i = 0; i < list.size(); i++)
                {
                    dictArray[i] = list.get(i);
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
            }
            dialogAddDictShow();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }
}
