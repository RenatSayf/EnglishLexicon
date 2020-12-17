package com.myapp.lexicon.playlist;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.PlayListBus;
import com.myapp.lexicon.main.MainViewModel;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


@AndroidEntryPoint
public class PlayList extends AppCompatActivity implements ListViewAdapter.IPlayListChangeListener
{
    private ListView listViewDict;
    private AppSettings appSettings;
    private PlayListFields m;
    private LockOrientation lockOrientation;

    private final String KEY_FIELDS = "key_fields";

    private final CompositeDisposable composite = new CompositeDisposable();
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.p_layout_play_list);
            Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null)
            {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

            lockOrientation = new LockOrientation(this);

            if (savedInstanceState == null)
            {
                m = new PlayListFields();
            }
            else
            {
                m = savedInstanceState.getParcelable(KEY_FIELDS);
            }



            appSettings = new AppSettings(PlayList.this);

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
                                appSettings.set_N_Word(1);
                                appSettings.set_N_Dict(0);
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
        } catch (Exception e)
        {
            e.printStackTrace();
            finish();
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
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
        if (lockOrientation != null)
        {
            lockOrientation.unLock();
        }
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        composite.dispose();
        composite.clear();
    }

    public void buttonAddClick(View view)
    {
        composite.add(
                mainViewModel.getDictList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(strings -> dialogAddDictShow((ArrayList<String>) strings), t -> {
                    dialogAddDictShow(new ArrayList<>());
                    Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
                })
        );
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
                    .setMultiChoiceItems(dictArray, choice, (dialog, which, isChecked) -> {
                        if (isChecked)
                        {
                            if (!m.newPlayList.contains(dictArray[which]))
                            {
                                m.newPlayList.add(dictArray[which]);
                            }
                        }
                        else
                        {
                            m.newPlayList.remove(dictArray[which]);
                        }
                    })
                    .setPositiveButton(getString(R.string.text_ok), (dialog, which) -> {
                        try
                        {
                            onPlayListChanged(m.newPlayList);
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        lockOrientation.unLock();
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
        if (newPlayList.size() > 0)
        {
            if (!newPlayList.contains(oldCurrentDict))
            {
                appSettings.set_N_Dict(0);
                appSettings.set_N_Word(1);
            } else
            {
                int nDict = newPlayList.indexOf(oldCurrentDict);
                if (AppData.getInstance().getNdict() != nDict)
                {
                    appSettings.set_N_Dict(nDict);
                    appSettings.set_N_Word(1);
                }
            }
        }
        PlayListBus.INSTANCE.update(newPlayList);
        ListViewAdapter lictViewAdapter = new ListViewAdapter(newPlayList, PlayList.this);
        listViewDict.setAdapter(lictViewAdapter);
        appSettings.savePlayList(newPlayList);

    }


}
