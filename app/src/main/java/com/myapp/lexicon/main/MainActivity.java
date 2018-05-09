package com.myapp.lexicon.main;

import android.annotation.SuppressLint;
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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.myapp.lexicon.R;
import com.myapp.lexicon.aboutapp.AboutAppFragment;
import com.myapp.lexicon.addword.AddWordActivity;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.database.GetEntriesLoader;
import com.myapp.lexicon.database.GetTableListFragm;
import com.myapp.lexicon.playlist.PlayList;
import com.myapp.lexicon.service.LexiconService;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;
import com.myapp.lexicon.settings.SettingsFragment;
import com.myapp.lexicon.wordeditor.WordEditor;
import com.myapp.lexicon.wordstests.Tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


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
    private TextView tvWordsCounter;
    private ImageButton btnPlay;
    private ImageButton btnStop;
    private ImageButton btnPause;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ProgressBar progressBar;
    private CheckBox checkBoxRuSpeak;
    private static Intent speechIntentService;
    public static Intent serviceIntent;
    private UpdateBroadcastReceiver mUpdateBroadcastReceiver;
    private boolean isFirstTime = true;
    private AppSettings appSettings;
    private AppData appData;
    private ArrayList<String> playList = new ArrayList<>();
    private DataBaseQueries dataBaseQueries;

    private final String KEY_ENG_TEXT = "eng_text";
    private final String KEY_RU_TEXT = "ru_text";
    private final String KEY_CURRENT_DICT = "current_dict";
    private final String KEY_TV_WORDS_COUNTER = "tv_words_counter";
    private final String KEY_BTN_PLAY_VISIBLE = "btn_play_visible";
    private final String KEY_BTN_PAUSE_VISIBLE = "btn_pause_visible";
    private final String KEY_BTN_STOP_VISIBLE = "btn_stop_visible";
    private final String KEY_BTN_NEXT_VISIBLE = "btn_next_visible";
    private final String KEY_BTN_BACK_VISIBLE = "btn_back_visible";
    private final String KEY_PROG_BAR_VISIBLE = "prog_bar_visible";

    private final int LOADER_GET_ENTRIES = 113336564;

    protected PowerManager.WakeLock wakeLock;

    private GetTableListFragm getTableListFragm;
    private FragmentManager fragmentManager;

    @SuppressLint("WakelockTimeout")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_navig_main);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null)
        {
            BackgroundFragm backgroundFragm = new BackgroundFragm();
            getSupportFragmentManager().beginTransaction().replace(R.id.background_fragment, backgroundFragm).commit();
        }

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null)
        {
            this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"my_tag");
        }
        this.wakeLock.acquire();

        Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        appSettings = new AppSettings(this);
        playList = appSettings.getPlayList();
        appData = AppData.getInstance();
        appData.initAllSettings(this);

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
            e.printStackTrace();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
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
            tvWordsCounter.setText(savedInstanceState.getString(KEY_TV_WORDS_COUNTER));
            btnPlay.setVisibility(savedInstanceState.getInt(KEY_BTN_PLAY_VISIBLE));
            btnStop.setVisibility(savedInstanceState.getInt(KEY_BTN_STOP_VISIBLE));
            btnPause.setVisibility(savedInstanceState.getInt(KEY_BTN_PAUSE_VISIBLE));
            btnNext.setVisibility(savedInstanceState.getInt(KEY_BTN_NEXT_VISIBLE));
            btnPrevious.setVisibility(savedInstanceState.getInt(KEY_BTN_BACK_VISIBLE));
            progressBar.setVisibility(savedInstanceState.getInt(KEY_PROG_BAR_VISIBLE));
        }

        // TODO: AsyncTaskLoader - 3. инициализация
        getLoaderManager().initLoader(LOADER_GET_ENTRIES, savedInstanceState, this);

        if (appData.isAdMob())
        {
            if (appData.isOnline(this))
            {
                if (savedInstanceState == null)
                {
                    MainBannerFragment bannerFragment = new MainBannerFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.banner_frame_main, bannerFragment).commit();
                }
            }
        }

    }

    public Action getAction()
    {
        return Actions.newView(getResources().getString(R.string.app_name), getResources().getString(R.string.app_link));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseAppIndex.getInstance().update();
        FirebaseUserActions.getInstance().start(getAction());
    }

    private void initViews()
    {
        textViewEn = findViewById(R.id.enTextView);
        textViewRu = findViewById(R.id.ruTextView);
        textViewDict = findViewById(R.id.textViewDict);
        tvWordsCounter = findViewById(R.id.tv_words_counter);
        btnPlay = findViewById(R.id.btn_play);
        btnStop = findViewById(R.id.btn_stop);
        if (btnStop != null)
        {
            btnStop.setVisibility(View.GONE);
        }
        btnPause = findViewById(R.id.btn_pause);
        if (btnPause != null)
        {
            btnPause.setVisibility(View.GONE);
        }
        btnPrevious = findViewById(R.id.btn_previous);
        if (btnPrevious != null)
        {
            btnPrevious.setVisibility(View.GONE);
        }
        btnNext = findViewById(R.id.btn_next);
        if (btnNext != null)
        {
            btnNext.setVisibility(View.GONE);
        }
        progressBar = findViewById(R.id.progressBar);
        checkBoxRuSpeak = findViewById(R.id.check_box_ru_speak);
        checkBoxRuSpeak.setChecked(appSettings.isEnglishSpeechOnly());
        switchRuSound_OnCheckedChange();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_ENG_TEXT, textViewEn.getText().toString());
        outState.putString(KEY_RU_TEXT, textViewRu.getText().toString());
        outState.putString(KEY_CURRENT_DICT, textViewDict.getText().toString());
        outState.putString(KEY_TV_WORDS_COUNTER, tvWordsCounter.getText().toString());
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
        appData.saveAllSettings(this);
        if (getInchesDisplay() < 7)
        {
            if (!isActivityOnTop())
            {
                speechServiceOnPause();
            }
        }
    }

    @Override
    protected void onStop()
    {
        progressBar.setVisibility(View.GONE);
        FirebaseUserActions.getInstance().end(getAction());
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
        appSettings = new AppSettings(this);
        playList = appSettings.getPlayList();
        appData = AppData.getInstance();
        appData.initAllSettings(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mUpdateBroadcastReceiver);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }

    @Override
    public void onBackPressed()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        speechServiceOnPause();
        appData.saveAllSettings(this);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        getMenuInflater().inflate(R.menu.a_up_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.edit_word)
        {
            if (playList != null && playList.size() > 0)
            {
                if (wordEditorIntent == null)
                {
                    wordEditorIntent = new Intent(this, WordEditor.class);
                }
                speechServiceOnPause();
                Bundle bundle = new Bundle();
                bundle.putString(WordEditor.KEY_EXTRA_DICT_NAME, playList.get(AppData.getInstance().getNdict()));
                bundle.putInt(WordEditor.KEY_ROW_ID, appData.getNword());
                wordEditorIntent.replaceExtras(bundle);

                startActivity(wordEditorIntent);
            }
            else
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
        }
        if (id == R.id.edit_speech_data)
        {
            speechServiceOnPause();
            Intent speechEditorIntent = new Intent(Intent.ACTION_VIEW);
            speechEditorIntent.setAction(Settings.ACTION_SETTINGS);
            startActivity(speechEditorIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
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
            Bundle bundle = new Bundle();
            if (playList.size() > 0)
            {
                bundle.putString(WordEditor.KEY_EXTRA_DICT_NAME, playList.get(AppData.getInstance().getNdict()));
                wordEditorIntent.replaceExtras(bundle);
            }
            startActivity(wordEditorIntent);
        }
        else if (id == R.id.nav_check_your_self)
        {
            if (testsIntent == null)
            {
                testsIntent = new Intent(this, Tests.class);
            }
            startActivity(testsIntent);
        }
        else if (id == R.id.nav_play_list)
        {
            if (playListIntent == null)
            {
                playListIntent = new Intent(this, PlayList.class);
            }
            startActivity(playListIntent);
        }
        else if (id == R.id.nav_settings)
        {
            getFragmentManager().beginTransaction().replace(R.id.settings_fragment, new SettingsFragment()).addToBackStack(null).commit();
        }
        else if (id == R.id.nav_evaluate_app)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.app_link)));
            startActivity(intent);
        }
        else if (id == R.id.nav_about_app)
        {
            AboutAppFragment aboutAppFragment = new AboutAppFragment();
            fragmentManager.beginTransaction().replace(R.id.about_app_fragment, aboutAppFragment).addToBackStack(null).commit();
        }
        else if (id == R.id.nav_exit)
        {
            SplashScreenActivity.speech.stop();
            SplashScreenActivity.speech.shutdown();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            wakeLock.release();
            this.finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        @SuppressWarnings("unchecked") ArrayList<String> arrayList = (ArrayList<String>) object;
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
                                        dataBaseQueries = new DataBaseQueries(MainActivity.this);
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
        final View view = getLayoutInflater().inflate(R.layout.a_dialog_add_dict, new LinearLayout(this), false);
        final EditText editText = view.findViewById(R.id.dialog_add_dict);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        new AlertDialog.Builder(this).setTitle(R.string.title_new_dictionary).setIcon(R.drawable.icon_book)
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
        playList = appSettings.getPlayList();
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
            btnNext.setVisibility(View.GONE);
            btnPrevious.setVisibility(View.GONE);
            textViewRu.setText(null);
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
        appData.setPause(true);
        if (speechIntentService != null)
        {
            stopService(speechIntentService);
            SpeechService.stopIntentService();
        }
        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        btnPrevious.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
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
        }
        textViewEn.setText(null);
        textViewRu.setText(null);
        textViewDict.setText(null);
        tvWordsCounter.setText(null);
        btnPlay.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        btnPrevious.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        appData.setNdict(0);
        appData.setNword(1);
    }

    public void btnNextBackClick(View view)
    {
        playList = appSettings.getPlayList();
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
        tvWordsCounter.setText(String.valueOf(appData.getNword()));
    }

    private void getNext()
    {
        if (playList != null && playList.size() == 0)
        {
            speechServiceOnStop();
            return;
        }
        dataBaseQueries = new DataBaseQueries(this);
        int wordsCount = dataBaseQueries.getCountEntriesSync(playList.get(appData.getNdict()));
        appData.setNword(appData.getNword()+1);
        if (playList.size() > 1)
        {
            if (appData.getNword() > wordsCount)
            {
                appData.setNword(1);
                appData.setNdict(appData.getNdict()+1);
                if (appData.getNdict() > playList.size()-1)
                {
                    appData.setNdict(0);
                }
            }
        }
        else if (playList.size() == 1)
        {
            if (appData.getNword() > wordsCount)
            {
                appData.setNword(1);
            }
        }

        // TODO: AsyncTaskLoader - 4. Передача параметров в AsyncTaskLoader
        Bundle loaderBundle = new Bundle();
        loaderBundle.putString(GetEntriesLoader.KEY_TABLE_NAME, playList.get(appData.getNdict()));
        loaderBundle.putInt(GetEntriesLoader.KEY_START_ID, appData.getNword());
        loaderBundle.putInt(GetEntriesLoader.KEY_END_ID, appData.getNword());

        // TODO: AsyncTaskLoader - 5. Запуск загрузки данных
        Loader<Cursor> dbLoader = getLoaderManager().restartLoader(LOADER_GET_ENTRIES, loaderBundle, this);
        dbLoader.forceLoad();
    }

    private void getPrevious()
    {
        if (playList != null && playList.size() == 0)
        {
            speechServiceOnStop();
            return;
        }
        dataBaseQueries = new DataBaseQueries(this);
        playList = appSettings.getPlayList();

        if (playList.size() > 1)
        {
            appData.setNword(appData.getNword()-1);
            if (appData.getNword() < 1)
            {
                appData.setNdict(appData.getNdict()-1);
                if (appData.getNdict() < 0)
                {
                    appData.setNdict(playList.size()-1);
                }
                int wordsCount = dataBaseQueries.getCountEntriesSync(playList.get(appData.getNdict()));
                appData.setNword(wordsCount);
            }
        }
        if (playList.size() == 1)
        {
            if (appData.getNword() <= 1)
            {
                int wordsCount = dataBaseQueries.getCountEntriesSync(playList.get(appData.getNdict()));
                appData.setNword(wordsCount);
            }
            else
            {
                appData.setNword(appData.getNword()-1);
            }
        }

        // TODO: AsyncTaskLoader - 4. Передача параметров в AsyncTaskLoader
        Bundle loaderBundle = new Bundle();
        loaderBundle.putString(GetEntriesLoader.KEY_TABLE_NAME, playList.get(appData.getNdict()));
        loaderBundle.putInt(GetEntriesLoader.KEY_START_ID, appData.getNword());
        loaderBundle.putInt(GetEntriesLoader.KEY_END_ID, appData.getNword());

        // TODO: AsyncTaskLoader - 5. Запуск загрузки данных
        Loader<Cursor> dbLoader = getLoaderManager().restartLoader(LOADER_GET_ENTRIES, loaderBundle, this);
        dbLoader.forceLoad();
    }

    public void switchRuSound_OnCheckedChange()
    {
        checkBoxRuSpeak.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if (isChecked)
                {
                    appSettings.setEnglishSpeechOnly(true);
                    SpeechService.setEnglishOnly(appSettings.isEnglishSpeechOnly());
                    Toast.makeText(MainActivity.this, R.string.text_ru_speech_on,Toast.LENGTH_SHORT).show();
                }
                else
                {
                    appSettings.setEnglishSpeechOnly(false);
                    SpeechService.setEnglishOnly(appSettings.isEnglishSpeechOnly());
                    Toast.makeText(MainActivity.this, R.string.text_ru_speech_off,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        if (serviceIntent != null)
        {
            stopService(serviceIntent);
        }
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isUseService = preferences.getBoolean("service", true);
        if (appSettings.getPlayList().size() > 0 && isUseService)
        {
            serviceIntent = new Intent(this, LexiconService.class);
            startService(serviceIntent);
        }
    }

    // TODO: ActivityManager.RunningAppProcessInfo Проверка, что активити находится на верху стека
    public boolean isActivityOnTop()
    {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Service.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = null;
        if (activityManager != null)
        {
            runningAppProcesses = activityManager.getRunningAppProcesses();
        }
        if (runningAppProcesses != null && runningAppProcesses.size() > 0)
        {
            String processName = runningAppProcesses.get(0).processName;
            String packageName = getApplicationInfo().packageName;
            return processName.equals(packageName);
        }
        return false;
    }

    // TODO: AsyncTaskLoader - 1. MainActivity реализует интерфейс LoaderManager.LoaderCallbacks

    @SuppressWarnings("unchecked")
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
            textViewDict.setText(playList.get(appData.getNdict()));

            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "main_activity");
            SplashScreenActivity.speech.setLanguage(Locale.US);
            //SplashScreenActivity.speech.setSpeechRate(0.5f);

            SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                SplashScreenActivity.speech.speak(dataBaseEntry.getEnglish(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
            } else
            {
                SplashScreenActivity.speech.speak(dataBaseEntry.getEnglish(), TextToSpeech.QUEUE_ADD, hashMap);
            }

            final DataBaseEntry finalDataBaseEntry = dataBaseEntry;
            SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
            {
                @Override
                public void onStart(String utteranceId)
                {

                }

                @Override
                public void onDone(String utteranceId)
                {
                    //boolean englishSpeechOnly = appSettings.isEnglishSpeechOnly();
                    if (utteranceId.equals("main_activity") && appSettings.isEnglishSpeechOnly())
                    {
                        SplashScreenActivity.speech.setLanguage(Locale.getDefault());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        {
                            SplashScreenActivity.speech.speak(finalDataBaseEntry.getTranslate(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                        } else
                        {
                            SplashScreenActivity.speech.speak(finalDataBaseEntry.getTranslate(), TextToSpeech.QUEUE_ADD, hashMap);
                        }
                    }
                    SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
                }

                @Override
                public void onError(String utteranceId)
                {

                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader loader)
    {

    }

    public void btnSpeak_OnClick(View view)
    {
        speechServiceOnPause();
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "btn_speak_onclick");
        SplashScreenActivity.speech.setLanguage(Locale.US);
        SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            SplashScreenActivity.speech.speak(textViewEn.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
        } else
        {
            SplashScreenActivity.speech.speak(textViewEn.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
        }
        SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
        {
            @Override
            public void onStart(String utteranceId)
            {

            }

            @Override
            public void onDone(String utteranceId)
            {
                if (utteranceId.equals("btn_speak_onclick") && appSettings.isEnglishSpeechOnly())
                {
                    SplashScreenActivity.speech.setLanguage(Locale.getDefault());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        SplashScreenActivity.speech.speak(textViewRu.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
                    } else
                    {
                        SplashScreenActivity.speech.speak(textViewRu.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
                    }
                }
                SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
            }

            @Override
            public void onError(String s)
            {

            }
        });
    }

    private double getInchesDisplay()
    {
        double screenInches;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;
        int heightPixels = displayMetrics.heightPixels;
        int densityDpi = displayMetrics.densityDpi;
        double width = (double) widthPixels/(double) densityDpi;
        double height = (double) heightPixels/(double) densityDpi;
        double x = Math.pow(width,2);
        double y = Math.pow(height,2);
        screenInches = Math.sqrt(x+y);
        return screenInches;
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
            int nword = MainActivity.this.appData.getNword();
            tvWordsCounter.setText(String.valueOf(nword));
            if (!textViewEn.getText().equals(""))
            {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

}


