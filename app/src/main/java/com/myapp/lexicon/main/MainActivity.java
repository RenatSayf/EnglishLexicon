package com.myapp.lexicon.main;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.myapp.lexicon.R;
import com.myapp.lexicon.addword.AddWordActivity;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.database.GetEntriesLoader;
import com.myapp.lexicon.database.GetTableListFragm;
import com.myapp.lexicon.helpers.MyLog;
import com.myapp.lexicon.playlist.PlayList;
import com.myapp.lexicon.settings.AppData2;
import com.myapp.lexicon.settings.AppSettings;
import com.myapp.lexicon.wordeditor.WordEditor;
import com.myapp.lexicon.wordstests.Tests;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

//import com.myapp.lexicon.database.GetDbEntriesLoader;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        GetTableListFragm.OnTableListListener
{
    public DatabaseHelper databaseHelper;
    private Intent addWordIntent;
    private Intent wordEditorIntent;
    private Intent testsIntent;
    private Intent playListIntent;
    private TextView textViewEn;
    private TextView textViewRu;
    private TextView textViewDict;
    private Button btnPlay;
    private Button btnStop;
    private Button btnPause;
    private Button btnNext;
    private Button btnPrevious;
    private ProgressBar progressBar;
    private Switch switchRuSound;
    private static Intent speechIntentService;
    private UpdateBroadcastReceiver mUpdateBroadcastReceiver;
    private boolean isFirstTime = true;
    private AppSettings appSettings;
    private AppData2 appData2;
    private ArrayList<String> playList = new ArrayList<>();
    private BackgroundFragm backgroundFragm;

    private String KEY_ENG_TEXT = "eng_text";
    private String KEY_RU_TEXT = "ru_text";
    private String KEY_CURRENT_DICT = "current_dict";
    private String KEY_BTN_PLAY_VISIBLE = "btn_play_visible";
    private String KEY_BTN_PAUSE_VISIBLE = "btn_pause_visible";
    private String KEY_BTN_STOP_VISIBLE = "btn_stop_visible";
    private String KEY_BTN_NEXT_VISIBLE = "btn_next_visible";
    private String KEY_BTN_BACK_VISIBLE = "btn_back_visible";
    private String KEY_PROG_BAR_VISIBLE = "prog_bar_visible";

    protected PowerManager.WakeLock wakeLock;

    private GetTableListFragm getTableListFragm;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_navig_main);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null)
        {
            backgroundFragm = new BackgroundFragm();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, backgroundFragm).addToBackStack(null).commit();
        }

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"my_tag");
        this.wakeLock.acquire();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        appSettings = new AppSettings(this);
        playList = appSettings.getPlayList();
        appData2 = AppData2.getInstance();
        appData2.initAllSettings(this);

        initViews();

        // Регистрируем приёмник
        mUpdateBroadcastReceiver = new UpdateBroadcastReceiver();
        IntentFilter updateIntentFilter = new IntentFilter(SpeechService.ACTION_UPDATE);
        updateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        try
        {
            registerReceiver(mUpdateBroadcastReceiver, updateIntentFilter);
        } catch (Exception e)
        {
            MyLog.v(e.getMessage());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null)
        {
            navigationView.setNavigationItemSelectedListener(this);
        }

        if (savedInstanceState == null && databaseHelper == null)
        {
            databaseHelper = new DatabaseHelper(this);
            databaseHelper.create_db();
        }

        if (savedInstanceState != null)
        {
            isFirstTime = false;
            textViewEn.setText(savedInstanceState.getString(KEY_ENG_TEXT));
            textViewRu.setText(savedInstanceState.getString(KEY_RU_TEXT));
            textViewDict.setText(savedInstanceState.getString(KEY_CURRENT_DICT));
            textViewDict.setVisibility(View.VISIBLE);
            btnPlay.setVisibility(savedInstanceState.getInt(KEY_BTN_PLAY_VISIBLE));
            btnStop.setVisibility(savedInstanceState.getInt(KEY_BTN_STOP_VISIBLE));
            btnPause.setVisibility(savedInstanceState.getInt(KEY_BTN_PAUSE_VISIBLE));
            btnNext.setVisibility(savedInstanceState.getInt(KEY_BTN_NEXT_VISIBLE));
            btnPrevious.setVisibility(savedInstanceState.getInt(KEY_BTN_BACK_VISIBLE));
            progressBar.setVisibility(savedInstanceState.getInt(KEY_PROG_BAR_VISIBLE));
        }

        // TODO: AsyncTaskLoader - 3. инициализация
        getLoaderManager().initLoader(LOADER_GET_ENTRIES, savedInstanceState, this);

    }


    private void initViews()
    {
        textViewEn = (TextView) findViewById(R.id.enTextView);
        textViewRu = (TextView) findViewById(R.id.ruTextView);
        textViewDict = (TextView) findViewById(R.id.textViewDict);
        if (textViewDict != null)
        {
            textViewDict.setVisibility(View.INVISIBLE);
        }
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnStop = (Button) findViewById(R.id.btn_stop);
        if (btnStop != null)
        {
            btnStop.setVisibility(View.GONE);
        }
        btnPause = (Button) findViewById(R.id.btn_pause);
        if (btnPause != null)
        {
            btnPause.setVisibility(View.GONE);
        }
        btnPrevious = (Button) findViewById(R.id.btn_previous);
        if (btnPrevious != null)
        {
            btnPrevious.setVisibility(View.GONE);
        }
        btnNext = (Button) findViewById(R.id.btn_next);
        if (btnNext != null)
        {
            btnNext.setVisibility(View.GONE);
        }
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        switchRuSound = (Switch) findViewById(R.id.switch_ru_sound);
        switchRuSound.setChecked(appSettings.isEnglishSpeechOnly());
        switchRuSound_OnCheckedChange();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_ENG_TEXT, textViewEn.getText().toString());
        outState.putString(KEY_RU_TEXT, textViewRu.getText().toString());
        outState.putString(KEY_CURRENT_DICT, textViewDict.getText().toString());
        outState.putInt(KEY_BTN_PLAY_VISIBLE, btnPlay.getVisibility());
        outState.putInt(KEY_BTN_STOP_VISIBLE, btnStop.getVisibility());
        outState.putInt(KEY_BTN_PAUSE_VISIBLE, btnPause.getVisibility());
        outState.putInt(KEY_BTN_NEXT_VISIBLE, btnNext.getVisibility());
        outState.putInt(KEY_BTN_BACK_VISIBLE, btnPrevious.getVisibility());
        outState.putInt(KEY_PROG_BAR_VISIBLE, progressBar.getVisibility());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        appData2.saveAllSettings(this);
        if (!isActivityOnTop())
        {
            speechServiceOnPause();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mUpdateBroadcastReceiver);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        if (!isActivityOnTop())
        {
            speechServiceOnPause();
        }
    }

    @Override
    public void onBackPressed()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        speechServiceOnPause();
        appData2.saveAllSettings(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
        {
            if (drawer.isDrawerOpen(GravityCompat.START))
            {
                drawer.closeDrawer(GravityCompat.START);
            } else
            {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.a_up_menu_main, menu);
        return true;
    }

    public static final String KEY_DICT_NAME = "key_name_dict";
    public static final String KEY_DICT_INDEX = "key_dict_index";
    public static final String KEY_ROW_ID = "key_row_id";

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.edit_word)
        {
            if (wordEditorIntent == null)
            {
                wordEditorIntent = new Intent(this, WordEditor.class);
            }
            speechServiceOnPause();
            Bundle bundle = new Bundle();
            bundle.putString(KEY_DICT_NAME, textViewDict.getText().toString());
            bundle.putInt(KEY_DICT_INDEX, appData2.getNdict());
            bundle.putInt(KEY_ROW_ID, appData2.getNword());
            wordEditorIntent.putExtras(bundle);

            startActivity(wordEditorIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        speechServiceOnPause();

        if (id == R.id.nav_add_word)
        {
            if (addWordIntent == null)
            {
                addWordIntent = new Intent(this,AddWordActivity.class);
            }
            startActivity(addWordIntent);
        }
        else if (id == R.id.nav_add_dict)
        {
            dialogAddDict();
        }
        else if (id == R.id.nav_delete_dict)
        {
            getTableListFragm = new GetTableListFragm();
            fragmentManager.beginTransaction().add(getTableListFragm, "get_table_list").commit();
        }
        else if (id == R.id.nav_edit)
        {
            if (wordEditorIntent == null)
            {
                wordEditorIntent = new Intent(this, WordEditor.class);
            }
            wordEditorIntent.replaceExtras(new Bundle());
            startActivity(wordEditorIntent);
        }
        else if (id == R.id.nav_check_your_self)
        {
            if (testsIntent == null)
            {
                testsIntent = new Intent(this, Tests.class);
            }
            startActivity(testsIntent);
        } else if (id == R.id.nav_play_list)
        {
            if (playListIntent == null)
            {
                playListIntent = new Intent(this, PlayList.class);
            }
            startActivity(playListIntent);
        } else if (id == R.id.nav_exit)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            wakeLock.release();
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null)
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    public void onGetTableListListener(Object object)
    {
        getTableListFragm = (GetTableListFragm) fragmentManager.findFragmentByTag("get_table_list");
        if (getTableListFragm != null)
        {
            fragmentManager.beginTransaction().remove(getTableListFragm).commit();
        }
        ArrayList<String> arrayList = (ArrayList<String>) object;
        final ArrayList<String> delete_items = new ArrayList<>();
        final String[] items = new  String[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++)
        {
            items[i] = arrayList.get(i);
        }
        boolean[]choice = new boolean[items.length];
        new AlertDialog.Builder(MainActivity.this).setTitle(R.string.title_del_dict)
                .setMultiChoiceItems(items, choice, new DialogInterface.OnMultiChoiceClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        delete_items.add(items[which]);
                        //Toast.makeText(a_MainActivity.this, "Добавлен элемент - " + delete_items.get(delete_items.size()-1), Toast.LENGTH_SHORT).show();
                    }
                }).setPositiveButton(R.string.button_text_delete, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(delete_items.size() <= 0)    return;
                new AlertDialog.Builder(MainActivity.this).setTitle(R.string.dialog_are_you_sure)
                        .setPositiveButton(R.string.button_text_yes, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                boolean result = false;
                                for (String item : delete_items)
                                {
                                    try
                                    {
                                        result = dataBaseQueries.deleteTableFromDbSync(item);
                                        appSettings.removeItemFromPlayList(item);
                                    } catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                if (result)
                                {
                                    Toast.makeText(MainActivity.this, R.string.msg_selected_dict_removed, Toast.LENGTH_LONG).show();
                                }
                            }
                        }).setNegativeButton(R.string.button_text_no, null).create().show();
            }
        }).setNegativeButton(R.string.button_text_cancel,null).create().show();
    }

    private void dialogAddDict()
    {
        final View view = getLayoutInflater().inflate(R.layout.a_dialog_add_dict, null);
        final EditText editText = (EditText) view.findViewById(R.id.dialog_add_dict);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        new AlertDialog.Builder(this).setTitle(R.string.title_new_dictionary).setIcon(R.drawable.icon_add_dict)
                .setPositiveButton(R.string.btn_text_add, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                        String dictName = editText.getText().toString();
                        if (!dictName.equals(""))
                        {
                            try
                            {
                                dataBaseQueries = new DataBaseQueries(MainActivity.this);
                                dataBaseQueries.addTableToDbSync(dictName);
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.text_added_new_dict)+dictName, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }
                })
                .setNegativeButton(R.string.btn_text_cancel, null)
                .setView(view).create().show();

        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String str = s.toString();
                for (int i = 0; i < str.length(); i++)
                {
                    int char_first = str.codePointAt(0);
                    if ((char_first >= 33 && char_first <= 64) || (char_first >= 91 && char_first <= 96) ||
                            (char_first >= 123 && char_first <= 126))
                    {
                        String str_wrong = str.substring(i);
                        editText.setText(str.replace(str_wrong,""));
                    }
                    if ((str.codePointAt(i) >= 33 && str.codePointAt(i) <= 47) || (str.codePointAt(i) >= 58 && str.codePointAt(i) <= 64) ||
                            (str.codePointAt(i) >= 91 && str.codePointAt(i) <= 96) || (str.codePointAt(i) >= 123 && str.codePointAt(i) <= 126))
                    {
                        String str_wrong = str.substring(i);
                        editText.setText(str.replace(str_wrong,""));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s){}
        });
    }

    public void btnPlayClick(View view)
    {
        if (playList.size() > 0)
        {
            if (isFirstTime)
            {
                Toast toast = Toast.makeText(this, R.string.message_about_start,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP,0,0);
                toast.show();
            }

            speechIntentService = new Intent(this, SpeechService.class);
            stopService(speechIntentService);
            speechIntentService.putExtra(getString(R.string.key_play_order), appSettings.getOrderPlay());
            speechIntentService.putExtra(getString(R.string.is_one_time), false);
            startService(speechIntentService);

            btnPlay.setVisibility(View.GONE);
            btnStop.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);
            btnPrevious.setVisibility(View.VISIBLE);
            textViewRu.setText(null);
            textViewDict.setVisibility(View.VISIBLE);
        } else
        {
            Toast toast = Toast.makeText(this, R.string.no_playlist, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
            if (playListIntent == null)
            {
                playListIntent = new Intent(this, PlayList.class);
            }
            startActivity(playListIntent);
        }
        progressBar.setVisibility(View.VISIBLE);

    }
    public void btnPauseClick(View view)
    {
        Toast toast = Toast.makeText(this, R.string.message_about_pause,Toast.LENGTH_SHORT);
        toast.show();
        speechServiceOnPause();

    }

    private void speechServiceOnPause()
    {
        appData2.setPause(true);
        if (speechIntentService != null)
        {
            stopService(speechIntentService);
        }
        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);
        textViewRu.setText(null);
    }

    public void btnStopClick(View view)
    {
        speechServiceOnStop();
    }

    private void speechServiceOnStop()
    {
        if (speechIntentService != null)
        {
            stopService(speechIntentService);
            SpeechService.resetCounter(false);
        }
        textViewEn.setText(null);
        textViewRu.setText(null);
        btnPlay.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnPrevious.setVisibility(View.GONE);
        textViewDict.setVisibility(View.INVISIBLE);
    }

    private DataBaseQueries dataBaseQueries;

    public void btnNextBackClick(View view) throws SQLException, ExecutionException, InterruptedException
    {
        speechServiceOnPause();
        int id = view.getId();
        if (id == R.id.btn_next)
        {
            getNext();
        }

        if (id == R.id.btn_previous)
        {
            getPrevious();
        }
    }

    private void getNext() throws SQLException, ExecutionException, InterruptedException
    {
        dataBaseQueries = new DataBaseQueries(this);
        int wordsCount = dataBaseQueries.getCountEntriesSync(playList.get(appData2.getNdict()));
        appData2.setNword(appData2.getNword()+1);
        if (playList.size() > 1)
        {
            if (appData2.getNword() > wordsCount)
            {
                appData2.setNword(1);
                appData2.setNdict(appData2.getNdict()+1);
                if (appData2.getNdict() > playList.size()-1)
                {
                    appData2.setNdict(0);
                }
            }
        }
        else if (playList.size() == 1)
        {
            if (appData2.getNword() > wordsCount)
            {
                appData2.setNword(1);
            }
        }

        // TODO: AsyncTaskLoader - 4. Передача параметров в AsyncTaskLoader
        Bundle loaderBundle = new Bundle();
        loaderBundle.putString(GetEntriesLoader.KEY_TABLE_NAME, playList.get(appData2.getNdict()));
        loaderBundle.putInt(GetEntriesLoader.KEY_START_ID, appData2.getNword());
        loaderBundle.putInt(GetEntriesLoader.KEY_END_ID, appData2.getNword());

        // TODO: AsyncTaskLoader - 5. Запуск загрузки данных
        Loader<Cursor> dbLoader = getLoaderManager().restartLoader(LOADER_GET_ENTRIES, loaderBundle, this);
        dbLoader.forceLoad();
    }

    private void getPrevious() throws SQLException, ExecutionException, InterruptedException
    {
        dataBaseQueries = new DataBaseQueries(this);
        playList = appSettings.getPlayList();

        if (playList.size() > 1)
        {
            appData2.setNword(appData2.getNword()-1);
            if (appData2.getNword() < 1)
            {
                appData2.setNdict(appData2.getNdict()-1);
                if (appData2.getNdict() < 0)
                {
                    appData2.setNdict(playList.size()-1);
                }
                int wordsCount = dataBaseQueries.getCountEntriesSync(playList.get(appData2.getNdict()));
                appData2.setNword(wordsCount);
            }
        }
        if (playList.size() == 1)
        {
            if (appData2.getNword() <= 1)
            {
                int wordsCount = dataBaseQueries.getCountEntriesSync(playList.get(appData2.getNdict()));
                appData2.setNword(wordsCount);
            }
            else
            {
                appData2.setNword(appData2.getNword()-1);
            }
        }

        // TODO: AsyncTaskLoader - 4. Передача параметров в AsyncTaskLoader
        Bundle loaderBundle = new Bundle();
        loaderBundle.putString(GetEntriesLoader.KEY_TABLE_NAME, playList.get(appData2.getNdict()));
        loaderBundle.putInt(GetEntriesLoader.KEY_START_ID, appData2.getNword());
        loaderBundle.putInt(GetEntriesLoader.KEY_END_ID, appData2.getNword());

        // TODO: AsyncTaskLoader - 5. Запуск загрузки данных
        Loader<Cursor> dbLoader = getLoaderManager().restartLoader(LOADER_GET_ENTRIES, loaderBundle, this);
        dbLoader.forceLoad();
    }

    public void switchRuSound_OnCheckedChange()
    {
        switchRuSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    appSettings.setEnglishSpeechOnly(true);
                    SpeechService.setEnglishOnly(appSettings.isEnglishSpeechOnly());
                    Toast toast = Toast.makeText(MainActivity.this,"Русскоязычное озвучивание включено",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();

                }
                else
                {
                    appSettings.setEnglishSpeechOnly(false);
                    SpeechService.setEnglishOnly(appSettings.isEnglishSpeechOnly());
                    Toast toast = Toast.makeText(MainActivity.this,"Русскоязычное озвучивание отключено",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
            }
        });

        switchRuSound.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
//                Toast toast = Toast.makeText(a_MainActivity.this,"Вы можете отключить русское озвучивание, что бы ускорить воспроизведение",Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.TOP, 0, 0);
//                toast.show();
            }
        });
    }

    // TODO: ActivityManager.RunningAppProcessInfo Проверка, что активити находится на верху стека
    public boolean isActivityOnTop()
    {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Service.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses.size() > 0)
        {
            String processName = runningAppProcesses.get(0).processName;
            String packageName = getApplicationInfo().packageName;
            if (processName.equals(packageName))
            {
                return true;
            }
        }
        return false;
    }

    // TODO: AsyncTaskLoader - 1. MainActivity реализует интерфейс LoaderManager.LoaderCallbacks
    private final int LOADER_GET_ENTRIES = 113336564;
    @Override
    public Loader onCreateLoader(int id, Bundle bundle)
    {
        Loader<Cursor> loader = null;
        switch (id)
        {
            case LOADER_GET_ENTRIES:
                loader = new GetEntriesLoader(this, bundle);
            default:
                break;
        }
        return loader;
    }

    @Override   // TODO: AsyncTaskLoader - 2. Реализация интерфейса LoaderManager.LoaderCallbacks
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        DataBaseEntry dataBaseEntry = null;
        try
        {
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                dataBaseEntry = new DataBaseEntry(cursor.getString(0), cursor.getString(1), cursor.getString(3));
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

        if (dataBaseEntry != null)
        {
            textViewEn.setText(dataBaseEntry.getEnglish());
            textViewRu.setText(dataBaseEntry.getTranslate());
            textViewDict.setText(playList.get(appData2.getNdict()));

            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "main_activity");
            SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                SplashScreenActivity.speech.speak(dataBaseEntry.getEnglish(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
            } else
            {
                SplashScreenActivity.speech.speak(dataBaseEntry.getEnglish(), TextToSpeech.QUEUE_ADD, hashMap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader)
    {

    }



    public class UpdateBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String updateEN = intent.getStringExtra(SpeechService.EXTRA_KEY_EN);
            String updateRU = intent.getStringExtra(SpeechService.EXTRA_KEY_RU);
            String updateDict = intent.getStringExtra(SpeechService.EXTRA_KEY_DICT);
            textViewEn.setText(updateEN);
            textViewRu.setText(updateRU);
            textViewDict.setText(updateDict);
            if (!textViewEn.getText().equals(""))
            {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}


