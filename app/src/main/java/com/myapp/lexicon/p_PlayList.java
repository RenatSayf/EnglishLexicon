package com.myapp.lexicon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class p_PlayList extends AppCompatActivity
{
    private Spinner spinneOrderPlay;
    private ListView listViewDict;
    private p_ListViewAdapter lictViewAdapter;
    private ArrayList<String> playList = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private AppSettings appSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.p_layout_play_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appSettings = new AppSettings(p_PlayList.this);

        if (databaseHelper == null)
        {
            databaseHelper = new DatabaseHelper(this);
            databaseHelper.create_db();
        }

        listViewDict = (ListView) findViewById(R.id.listView_playList);
        spinneOrderPlay = (Spinner) findViewById(R.id.spinner_order_play);
        spinneOrderPlay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {
                    case 0:

                        break;
                    case 1:

                        break;
                    case 2:

                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.key_play_list), MODE_PRIVATE);
        String play_list_items = sharedPreferences.getString(getString(R.string.play_list_items), null);

        if (play_list_items != null && play_list_items.length() > 0)
        {
            String[] splitArray = play_list_items.split(" ");
            for (int i = 0; i < splitArray.length; i++)
            {
                playList.add(i, splitArray[i]);
            }
        }

        if (playList.size() > 0)
        {
            lictViewAdapter = new p_ListViewAdapter(playList, p_PlayList.this);
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

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        a_MainActivity.savePlayList(playList);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        a_MainActivity.savePlayList(playList);
    }

    private String[] dictArray;

    public void buttonAddClick(View view)
    {
        z_GetListTableFromDbAsync getListTableFromDbAsync = new z_GetListTableFromDbAsync(this);
        getListTableFromDbAsync.execute();
        try
        {
            dictArray = getListTableFromDbAsync.get();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }

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
                            lictViewAdapter = new p_ListViewAdapter(playList, p_PlayList.this);
                            listViewDict.setAdapter(lictViewAdapter);
                            appSettings.savePlayList(playList);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).create().show();
    }

    public void onClickCheckBoxItem(View view)
    {
        lictViewAdapter = new p_ListViewAdapter(playList, p_PlayList.this);
        listViewDict.setAdapter(lictViewAdapter);
    }

}
