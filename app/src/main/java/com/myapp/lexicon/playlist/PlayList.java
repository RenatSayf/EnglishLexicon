package com.myapp.lexicon.playlist;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.database.GetTableListFragm;
import com.myapp.lexicon.database.UpdateDBEntryAsync;
import com.myapp.lexicon.dialogs.InclusionDialog;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;

public class PlayList extends AppCompatActivity implements ListViewAdapter.IPlayListChangeListener, InclusionDialog.IInclusionDialog, GetTableListFragm.OnTableListListener
{
    private ListView listViewDict;
    private ListViewAdapter lictViewAdapter;
    private DatabaseHelper databaseHelper;
    private AppSettings appSettings;
    private PlayListFields m;
    private LockOrientation lockOrientation;

    private final String KEY_FIELDS = "key_fields";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.p_layout_play_list);
        Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
        setSupportActionBar(toolbar);

        lockOrientation = new LockOrientation(this);

        if (savedInstanceState == null)
        {
            m = new PlayListFields();
        }
        else
        {
            m = savedInstanceState.getParcelable(KEY_FIELDS);
        }

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        appSettings = new AppSettings(PlayList.this);

        if (databaseHelper == null)
        {
            databaseHelper = new DatabaseHelper(this);
            databaseHelper.create_db();
        }

        listViewDict = findViewById(R.id.listView_playList);
        Spinner spinneOrderPlay = findViewById(R.id.spinner_order_play);
        if (spinneOrderPlay != null)
        {
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
                            appSettings.setWordNumber(1);
                            appSettings.setDictNumber(0);
                            break;
                        case 2:

                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                }
            });
        }

        m.newPlayList = appSettings.getPlayList();

        if (m.newPlayList.size() > 0)
        {
            onPlayListChanged(m.newPlayList);
        }

        if (savedInstanceState == null)
        {
            if (AppData.getInstance().isAdMob())
            {
                if (AppData.getInstance().isOnline(this))
                {
                    BannerFragmentPL bannerFragment = new BannerFragmentPL();
                    getSupportFragmentManager().beginTransaction().replace(R.id.banner_frame_pl, bannerFragment).commit();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_FIELDS, m);
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
        lockOrientation.unLock();
    }

    public void buttonAddClick(View view)
    {
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(GetTableListFragm.TAG);
        if (fragmentByTag != null)
        {
            getSupportFragmentManager().beginTransaction().remove(fragmentByTag).commit();
        }
        GetTableListFragm getTableListFragm = new GetTableListFragm();
        getSupportFragmentManager().beginTransaction().add(getTableListFragm, GetTableListFragm.TAG).commit();
    }

    private void dialogAddDictShow(final ArrayList<String> dictsList)
    {
        try
        {
            lockOrientation.lock();
            boolean[] choice = new boolean[dictsList.size()];
            final String[] dictArray = new String[dictsList.size()];
            for (int i = 0; i < dictsList.size(); i++)
            {
                dictArray[i] = dictsList.get(i);
            }
            new AlertDialog.Builder(this).setTitle(R.string.access_dict)
                    .setMultiChoiceItems(dictArray, choice, new DialogInterface.OnMultiChoiceClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked)
                        {
                            if (isChecked)
                            {
                                if (!m.newPlayList.contains(dictArray[which]))
                                {
                                    m.newPlayList.add(dictArray[which]);
                                }
                            }
                            else
                            {
                                if (m.newPlayList.contains(dictArray[which]))
                                {
                                    m.newPlayList.remove(dictArray[which]);
                                }
                            }
                        }
                    })
                    .setPositiveButton(getString(R.string.text_ok), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                onPlayListChanged(m.newPlayList);
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            lockOrientation.unLock();
                        }
                    })
                    .create().show();
        } catch (Exception e)
        {
            e.printStackTrace();
            lockOrientation.unLock();
        }
    }

    @Override
    public void onPlayListChanged(final ArrayList<String> newPlayList)
    {
        final String oldCurrentDict = appSettings.getCurrentDict();
        DataBaseQueries dataBaseQueries = new DataBaseQueries(this);

        ArrayList<String> studiedDicts = dataBaseQueries.getStudiedDicts(newPlayList);
        if (studiedDicts.size() > 0)
        {
            InclusionDialog dialog = InclusionDialog.getInstance(studiedDicts);
            dialog.setResultListener(this);
            dialog.show(getSupportFragmentManager(), InclusionDialog.TAG);
        } else
        {
            if (newPlayList.size() > 0)
            {
                if (!newPlayList.contains(oldCurrentDict))
                {
                    appSettings.setDictNumber(0);
                    appSettings.setWordNumber(1);
                } else
                {
                    int nDict = newPlayList.indexOf(oldCurrentDict);
                    if (AppData.getInstance().getNdict() != nDict)
                    {
                        appSettings.setDictNumber(nDict);
                        appSettings.setWordNumber(1);
                    }
                }
            }
            lictViewAdapter = new ListViewAdapter(newPlayList, PlayList.this);
            listViewDict.setAdapter(lictViewAdapter);
            appSettings.savePlayList(newPlayList);
        }
    }

    private int updateCounter;
    @Override
    public void inclusionDialogResult(final ArrayList<String> studiedDicts, int result)
    {
        switch (result)
        {
            case -1:
                for (String item : studiedDicts)
                {
                    m.newPlayList.remove(item);
                }
                lictViewAdapter = new ListViewAdapter(m.newPlayList, PlayList.this);
                listViewDict.setAdapter(lictViewAdapter);
                appSettings.savePlayList(m.newPlayList);
                break;
            case 1:
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_Count_REPEAT, 1);
                updateCounter = 0;
                for (final String item : studiedDicts)
                {
                    UpdateDBEntryAsync updateDBEntryAsync = new UpdateDBEntryAsync(PlayList.this, item, values, null, null, new UpdateDBEntryAsync.IUpdateDBListener()
                    {
                        @Override
                        public void updateDBEntry_OnComplete(int rows)
                        {
                            if (rows >= 0)
                            {
                                updateCounter++;
                                if (updateCounter == studiedDicts.size())
                                {
                                    lictViewAdapter = new ListViewAdapter(m.newPlayList, PlayList.this);
                                    listViewDict.setAdapter(lictViewAdapter);
                                    appSettings.savePlayList(m.newPlayList);
                                }
                                else
                                {
                                    Toast.makeText(PlayList.this, R.string.text_not_all_words_were_included, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                    if (updateDBEntryAsync.getStatus() != AsyncTask.Status.RUNNING)
                    {
                        updateDBEntryAsync.execute();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onGetTableListListener(Object object)
    {
        @SuppressWarnings("unchecked") ArrayList<String> arrayList = (ArrayList<String>) object;
        dialogAddDictShow(arrayList);
    }
}
