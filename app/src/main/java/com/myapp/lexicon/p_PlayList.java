package com.myapp.lexicon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class p_PlayList extends AppCompatActivity
{
    private Spinner _spinner_order_play;
    private ListView listViewDict;
    private p_ListViewAdapter lictViewAdapter;
    private ArrayList<p_ItemListDict> play_list = new ArrayList<p_ItemListDict>();
    private DatabaseHelper _databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.p_layout_play_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (_databaseHelper == null)
        {
            _databaseHelper = new DatabaseHelper(this);
            _databaseHelper.create_db();
        }

        listViewDict = (ListView) findViewById(R.id.listView_playList);
        _spinner_order_play = (Spinner) findViewById(R.id.spinner_order_play);
        _spinner_order_play.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                switch (position)
                {
                    case 0:
                        Toast.makeText(getBaseContext(), "Вбран элемент = " + position, Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getBaseContext(), "Вбран элемент = " + position, Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getBaseContext(), "Вбран элемент = " + position, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        lictViewAdapter = new p_ListViewAdapter(a_MainActivity.getPlayList(), p_PlayList.this);
        listViewDict.setAdapter(lictViewAdapter);

        listViewDict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Log.i("Lexicon", "Вход в p_PlayList.listViewDict.setOnItemSelectedListener() position = " + position);
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
        Log.i("Lexicon", "Вход в p_PlayList.onPause()");
        //a_MainActivity.databaseHelper.close();
        a_MainActivity.savePlayList(play_list);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i("Lexicon", "Вход в p_PlayList.onDestroy()");
        //a_MainActivity.databaseHelper.close();
        a_MainActivity.savePlayList(play_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds _items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.p_play_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String idStr = Integer.toString(id);
        //noinspection SimplifiableIfStatement

        switch (id)
        {
            case R.id.action_item1:

                break;
            case R.id.action_item2:

                break;
            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private String[] _items;
    Set<String> dict_for_storage = new HashSet<String>();

    public void buttonAddClick(View view)
    {
        z_GetListTableFromDbAsync getListTableFromDbAsync = new z_GetListTableFromDbAsync(this);
        getListTableFromDbAsync.execute();
        try
        {
            _items = getListTableFromDbAsync.get();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (ExecutionException e)
        {
            e.printStackTrace();
        }

        Log.i("Lexicon", "buttonAddClick _items = " + _items);
        boolean[] choice = new boolean[_items.length];
        new AlertDialog.Builder(this).setTitle(R.string.access_dict)
                .setMultiChoiceItems(_items, choice, new DialogInterface.OnMultiChoiceClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        Toast.makeText(getBaseContext(), R.string.select_dict + " - " + _items[which], Toast.LENGTH_SHORT).show();
                        play_list.add(new p_ItemListDict(_items[which], true));
                        a_MainActivity.savePlayList(play_list);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            lictViewAdapter = new p_ListViewAdapter(a_MainActivity.getPlayList(), p_PlayList.this);
                            listViewDict.setAdapter(lictViewAdapter);
                        } catch (Exception e)
                        {
                            Log.i("Lexicon", "Исключение в p_PlayList.buttonAddClick() setPositiveButton.onClick() = " + e);
                            e.printStackTrace();
                        }
                    }
                }).create().show();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }
    public void onClickCheckBoxItem(View view)
    {
        Log.i("Lexicon", "Вход в p_PlayList.onClickCheckBoxItem()" );
        lictViewAdapter = new p_ListViewAdapter(a_MainActivity.getPlayList(), p_PlayList.this);
        listViewDict.setAdapter(lictViewAdapter);
    }

}
