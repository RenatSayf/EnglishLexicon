package com.myapp.lexicon.main;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.myapp.lexicon.R;
import com.myapp.lexicon.aboutapp.AboutAppFragment;
import com.myapp.lexicon.addword.AddWordActivity;
import com.myapp.lexicon.cloudstorage.StorageFragment;
import com.myapp.lexicon.database.DataBaseEntry;
import com.myapp.lexicon.database.DataBaseQueries;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.database.GetTableListFragm;
import com.myapp.lexicon.helpers.Share;
import com.myapp.lexicon.playlist.PlayList;
import com.myapp.lexicon.service.LexiconService;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;
import com.myapp.lexicon.settings.SettingsFragment;
import com.myapp.lexicon.wordeditor.WordEditor;
import com.myapp.lexicon.wordstests.Tests;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        AppData.IDictNumChangeListener,
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
    private ProgressBar progressBar;
    private CheckBox checkBoxRuSpeak;
    private ImageView orderPlayIconIV;
    private static Intent speechIntentService;
    public static Intent serviceIntent;
    private SpeechServiceReceiver speechServiceReceiver;
    private boolean isFirstTime = true;
    private AppSettings appSettings;
    private AppData appData;
    private ArrayList<String> playList = new ArrayList<>();
    private DataBaseQueries dataBaseQueries;
    private Locale localeDefault;

    private final String KEY_ENG_TEXT = "eng_text";
    private final String KEY_RU_TEXT = "ru_text";
    private final String KEY_CURRENT_DICT = "current_dict";
    private final String KEY_TV_WORDS_COUNTER = "tv_words_counter";
    private final String KEY_BTN_PLAY_VISIBLE = "btn_play_visible";
    private final String KEY_BTN_PAUSE_VISIBLE = "btn_pause_visible";
    private final String KEY_BTN_STOP_VISIBLE = "btn_stop_visible";
    private final String KEY_PROG_BAR_VISIBLE = "prog_bar_visible";

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
            BackgroundFragm backgroundFragm = new BackgroundFragm();
            getSupportFragmentManager().beginTransaction().replace(R.id.background_fragment, backgroundFragm).commit();
        }

        Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
        setSupportActionBar(toolbar);

        appSettings = new AppSettings(this);
        playList = appSettings.getPlayList();
        appData = AppData.getInstance();
        appData.initAllSettings(this);

        initViews();

        // Регистрируем приёмник
        speechServiceReceiver = new SpeechServiceReceiver();
        IntentFilter updateIntentFilter = new IntentFilter(SpeechService.ACTION_UPDATE);
        updateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        try
        {
            registerReceiver(speechServiceReceiver, updateIntentFilter);
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
            progressBar.setVisibility(savedInstanceState.getInt(KEY_PROG_BAR_VISIBLE));
        }

        if (appData.isAdMob())
        {
            if (appData.isOnline(this))
            {
                if (savedInstanceState == null)
                {
                    MobileAds.initialize(this, getString(R.string.admob_app_id));
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
        EventBus.getDefault().post(new MainActivityOnStart(serviceIntent));
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
        progressBar = findViewById(R.id.progressBar);
        checkBoxRuSpeak = findViewById(R.id.check_box_ru_speak);
        checkBoxRuSpeak.setChecked(appSettings.isEnglishSpeechOnly());
        switchRuSound_OnCheckedChange();

        orderPlayIconIV = findViewById(R.id.order_play_icon_iv);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_ENG_TEXT, textViewEn.getText().toString());
        outState.putString(KEY_RU_TEXT, textViewRu.getText().toString());
        outState.putString(KEY_CURRENT_DICT, textViewDict.getText().toString());
        outState.putString(KEY_TV_WORDS_COUNTER, tvWordsCounter.getText().toString());
        outState.putInt(KEY_BTN_PLAY_VISIBLE, btnPlay.getVisibility());
        outState.putInt(KEY_BTN_STOP_VISIBLE, btnStop.getVisibility());
        outState.putInt(KEY_BTN_PAUSE_VISIBLE, btnPause.getVisibility());
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
        appData.setDictNumberChangeListener(this);
        if (appSettings.getOrderPlay() == 0)
        {
            orderPlayIconIV.setImageResource(R.drawable.ic_repeat_white);
        }
        if (appSettings.getOrderPlay() == 1)
        {
            orderPlayIconIV.setImageResource(R.drawable.ic_shuffle_white);
        }
        if (playList.size() > 0 && appData.getNdict() < playList.size())
        {
            textViewDict.setText(playList.get(appData.getNdict()));
        }
        else
        {
            textViewDict.setText("");
        }
        localeDefault = new Locale(appSettings.getTranslateLang());
        dataBaseQueries = new DataBaseQueries(this);
        if (serviceIntent != null)
        {
            stopService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (databaseHelper != null)
        {
            databaseHelper.close();
        }
        unregisterReceiver(speechServiceReceiver);
        if (AppData.getInstance().getDisplayVariant() == 1 && serviceIntent != null)
        {
            stopService(serviceIntent);
        }
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
                bundle.putString(WordEditor.KEY_EXTRA_DICT_NAME, textViewDict.getText().toString());
                String text = tvWordsCounter.getText().toString();
                try
                {
                    String[] splitArray = text.split(" ");
                    if (splitArray.length > 0)
                    {
                        bundle.putInt(WordEditor.KEY_ROW_ID, Integer.parseInt(splitArray[0]));
                    }

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
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

        if (id == R.id.cloud_storage)
        {
            StorageFragment storageFragment = StorageFragment.newInstance(null, null);
            fragmentManager.beginTransaction().replace(R.id.frame_to_page_fragm, storageFragment).addToBackStack(null).commit();
        }

        if (id == R.id.menu_item_share)
        {
            new Share().doShare(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
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
            fragmentManager.beginTransaction().add(getTableListFragm, GetTableListFragm.TAG).commit();
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
            getFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, new SettingsFragment()).addToBackStack(null).commit();
        }
        else if (id == R.id.nav_evaluate_app)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.app_link)));
            startActivity(intent);
        }
        else if (id == R.id.nav_share)
        {
            new Share().doShare(this);
        }
        else if (id == R.id.nav_about_app)
        {
            AboutAppFragment aboutAppFragment = new AboutAppFragment();
            fragmentManager.beginTransaction().replace(R.id.frame_to_page_fragm, aboutAppFragment).addToBackStack(null).commit();
        }
        else if (id == R.id.nav_exit)
        {
            if (SplashScreenActivity.speech != null)
            {
                SplashScreenActivity.speech.stop();
                SplashScreenActivity.speech.shutdown();
            }
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

            btnPlay.setVisibility(View.INVISIBLE);
            btnStop.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.VISIBLE);
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
        btnPlay.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        appData.setNdict(0);
        appData.setNword(1);
    }

    public void btnNextBackClick(View view)
    {
        playList = appSettings.getPlayList();
        if (playList == null || playList.size() == 0)
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
        speechServiceOnPause();
        if (SplashScreenActivity.speech != null)
        {
            SplashScreenActivity.speech.stop();
        }
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

    private void getNext()
    {
        if (playList != null && playList.size() == 0)
        {
            speechServiceOnStop();
            return;
        }

        appData.getNextNword(this, new AppData.IGetWordListerner()
        {
            @Override
            public void getWordComplete(ArrayList<DataBaseEntry> entries, Integer[] dictSize)
            {
                if (entries.size() > 0)
                {
                    DataBaseEntry dataBaseEntry = entries.get(0);
                    textViewEn.setText(dataBaseEntry.getEnglish());
                    textViewRu.setText(dataBaseEntry.getTranslate());
                    textViewDict.setText(playList.get(appData.getNdict()));
                    String concatText = (dataBaseEntry.getRowId() + "").concat(" / ").concat(Integer.toString(dictSize[1])).concat("  " + getString(R.string.text_studied) + " " + dictSize[2]);
                    tvWordsCounter.setText(concatText);

                    final HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "main_activity");
                    try
                    {
                        SplashScreenActivity.speech.setLanguage(Locale.US);
                    } catch (Exception e)
                    {
                        return;
                    }
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
                            if (utteranceId.equals("main_activity") && appSettings.isEnglishSpeechOnly())
                            {
                                SplashScreenActivity.speech.setLanguage(localeDefault);
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
                        public void onError(String utteranceId)
                        {

                        }
                    });
                }
            }
        });
    }

    private void getPrevious()
    {
        if (playList != null && playList.size() == 0)
        {
            speechServiceOnStop();
            return;
        }

        appData.getPreviousNword(this, new AppData.IGetWordListerner()
        {
            @Override
            public void getWordComplete(ArrayList<DataBaseEntry> entries, Integer[] dictSize)
            {
                if (entries.size() > 0)
                {
                    DataBaseEntry dataBaseEntry = entries.get(0);

                    textViewEn.setText(dataBaseEntry.getEnglish());
                    textViewRu.setText(dataBaseEntry.getTranslate());
                    textViewDict.setText(playList.get(appData.getNdict()));
                    String concatText = (dataBaseEntry.getRowId() + "").concat(" / ").concat(Integer.toString(dictSize[1])).concat("  " + getString(R.string.text_studied) + " " + dictSize[2]);
                    tvWordsCounter.setText(concatText);

                    final HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "main_activity");
                    try
                    {
                        SplashScreenActivity.speech.setLanguage(Locale.US);
                    } catch (Exception e)
                    {
                        return;
                    }
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
                            if (utteranceId.equals("main_activity") && appSettings.isEnglishSpeechOnly())
                            {
                                SplashScreenActivity.speech.setLanguage(localeDefault);
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
                        public void onError(String utteranceId)
                        {

                        }
                    });
                }
            }
        });
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
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isUseService = preferences.getBoolean("service", true);
        if (appSettings.getPlayList().size() > 0 && isUseService)
        {
            if (serviceIntent == null)
            {
                serviceIntent = new Intent(this, LexiconService.class);
            }
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

    public void btnSpeak_OnClick(View view)
    {
        speechServiceOnPause();
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "btn_speak_onclick");
        try
        {
            SplashScreenActivity.speech.setLanguage(Locale.US);
        } catch (Exception e)
        {
            return;
        }
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
                    SplashScreenActivity.speech.setLanguage(localeDefault);
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

    @Override
    public void dictNumberOnChanged(int ndict)
    {

    }

    public class SpeechServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String updateEN = intent.getStringExtra(SpeechService.EXTRA_KEY_EN);
            String updateRU = intent.getStringExtra(SpeechService.EXTRA_KEY_RU);
            String updateDict = intent.getStringExtra(SpeechService.EXTRA_KEY_DICT);
            String nword = intent.getStringExtra(SpeechService.EXTRA_KEY_WORDS_COUNTER);
            textViewEn.setText(updateEN);
            textViewRu.setText(updateRU);
            textViewDict.setText(updateDict);
            tvWordsCounter.setText(nword);
            if (!textViewEn.getText().equals(""))
            {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

}


