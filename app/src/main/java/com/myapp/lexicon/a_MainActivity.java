package com.myapp.lexicon;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.NavigationView;
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
import android.widget.ViewFlipper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class a_MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    public static SharedPreferences savedPlayList;
    public static SharedPreferences settings;
    public static String KEY_PLAY_LIST = "play_list";
    public static String KEY_ENG_ONLY = "eng_only";
    public DatabaseHelper databaseHelper;

    private Intent addWord;
    private Intent wordEditor;
    private Intent checkSelf;
    private Intent playList;
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
    private z_BackgroundAnim backgroundAnim;
    private boolean isFirstTime = true;

    private String KEY_ENG_TEXT = "eng_text";
    private String KEY_RU_TEXT = "ru_text";
    private String KEY_CURRENT_DICT = "current_dict";
    private String KEY_BTN_PLAY_VISIBLE = "btn_play_visible";
    private String KEY_BTN_PAUSE_VISIBLE = "btn_pause_visible";
    private String KEY_BTN_STOP_VISIBLE = "btn_stop_visible";
    private String KEY_BTN_NEXT_VISIBLE = "btn_next_visible";
    private String KEY_BTN_BACK_VISIBLE = "btn_back_visible";
    private String KEY_PROG_BAR_VISIBLE = "prog_bar_visible";

    private static int btnPlayVisible = View.VISIBLE;

    protected PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_navig_main);

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"my_tag");
        this.wakeLock.acquire();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        savedPlayList = getSharedPreferences(KEY_PLAY_LIST, MODE_PRIVATE);
        settings = getSharedPreferences(KEY_ENG_ONLY, MODE_PRIVATE);

        initViews();

        // Регистрируем приёмник
        mUpdateBroadcastReceiver = new UpdateBroadcastReceiver();
        IntentFilter updateIntentFilter = new IntentFilter(z_speechService.ACTION_UPDATE);
        updateIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        try
        {
            registerReceiver(mUpdateBroadcastReceiver, updateIntentFilter);
        } catch (Exception e)
        {
            z_Log.v(e.getMessage());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

    }
    private void initViews()
    {
        backgroundAnim = new z_BackgroundAnim(this, (ViewFlipper) findViewById(R.id.view_flipper));
        backgroundAnim.startAnimByRandom();
        textViewEn = (TextView) findViewById(R.id.enTextView);
        textViewRu = (TextView) findViewById(R.id.ruTextView);
        textViewDict = (TextView) findViewById(R.id.textViewDict);
        textViewDict.setVisibility(View.INVISIBLE);
        btnPlay = (Button) findViewById(R.id.btn_play);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setVisibility(View.GONE);
        btnPause = (Button) findViewById(R.id.btn_pause);
        btnPause.setVisibility(View.GONE);
        btnPrevious = (Button) findViewById(R.id.btn_previous);
        btnPrevious.setVisibility(View.GONE);
        btnNext = (Button) findViewById(R.id.btn_next);
        btnNext.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        switchRuSound = (Switch) findViewById(R.id.switch_ru_sound);
        switchRuSound.setChecked(settings.getBoolean(KEY_ENG_ONLY, true));
        switchRuSound_OnCheckedChange();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        backgroundAnim.onSaveInstanceState(null);
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
    }

    @Override
    public void onBackPressed()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        backgroundAnim.onDestroy();
        speechServiceOnPause();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.a_up_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_word)
        {
            if (addWord == null)
            {
                addWord = new Intent(this,b_AddWordActivity.class);
            }
            startActivity(addWord);
        }
        else if (id == R.id.nav_add_dict)
        {
            dialogAddDict();
        }
        else if (id == R.id.nav_delete_dict)
        {
            try
            {
                dialogDeleteDict();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else if (id == R.id.nav_edit)
        {
            if (wordEditor == null)
            {
                wordEditor = new Intent(this, d_WordEditor.class);
            }
            startActivity(wordEditor);
        }
        else if (id == R.id.nav_check_your_self)
        {
            if (checkSelf == null)
            {
                checkSelf = new Intent(this, t_Tests.class);
            }
            speechServiceOnPause();
            startActivity(checkSelf);
        } else if (id == R.id.nav_play_list)
        {
            if (playList == null)
            {
                playList = new Intent(this, p_PlayList.class);
            }
            startActivity(playList);
        } else if (id == R.id.nav_exit)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            backgroundAnim.onDestroy();
            speechServiceOnStop();
            wakeLock.release();
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private String[] items = new  String[0];

    private void dialogDeleteDict() throws SQLException
    {
        final ArrayList<String> delete_items = new ArrayList<>();
        final DataBaseQueries dataBaseQueries = new DataBaseQueries(this);
        try
        {
            items = dataBaseQueries.setListTableToSpinner();
        } catch (Exception e)
        {
            z_Log.v("Исключение - "+e.getMessage());
        }
        boolean[]choice = new boolean[items.length];
        new AlertDialog.Builder(this).setTitle(R.string.title_del_dict)
                .setMultiChoiceItems(items, choice, new DialogInterface.OnMultiChoiceClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked)
                    {
                        delete_items.add(items[which]);
                        Toast.makeText(a_MainActivity.this, "Добавлен элемент - " + delete_items.get(delete_items.size()-1), Toast.LENGTH_SHORT).show();
                    }
                }).setPositiveButton(R.string.button_text_delete, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(delete_items.size() <= 0)    return;
                new AlertDialog.Builder(a_MainActivity.this).setTitle(R.string.dialog_are_you_sure)
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
                                        result = dataBaseQueries.deleteTableFromDbAsync(item);
                                        removeItemPlayList(item);
                                    } catch (Exception e)
                                    {
                                        z_Log.v("Исключение - " + e.getMessage() + "\nresult = " + result);
                                        e.printStackTrace();
                                    }
                                }
                                if (result)
                                {
                                    Toast.makeText(a_MainActivity.this, R.string.msg_selected_dict_removed, Toast.LENGTH_LONG).show();
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
                                dataBaseQueries = new DataBaseQueries(a_MainActivity.this);
                                dataBaseQueries.addTableToDbAsync(dictName);
                            } catch (Exception e)
                            {
                                z_Log.v("Возникло исключение - "+e.getMessage());
                            }
                            Toast toast = Toast.makeText(getApplicationContext(), "Добавлен новый словарь: "+dictName, Toast.LENGTH_LONG);
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
    public static void savePlayList(ArrayList<String> addDicts)
    {
        Set<String> existDicts = a_MainActivity.savedPlayList.getStringSet(KEY_PLAY_LIST, new HashSet<String>());
        for (int i = 0; i < addDicts.size(); i++)
        {
            existDicts.add(addDicts.get(i));
        }
        savedPlayList.edit().remove(KEY_PLAY_LIST).commit();
        savedPlayList.edit().putStringSet(KEY_PLAY_LIST, existDicts).commit();
    }
    public static ArrayList<String> getPlayList()
    {
        ArrayList<String> listDicts=new ArrayList<>();
        Set<String> listKeptDict = savedPlayList.getStringSet(KEY_PLAY_LIST, new HashSet<String>());
        for (String item : listKeptDict)
        {
            listDicts.add(item);
        }

        return listDicts;
    }
    public static boolean removeItemPlayList(String name)
    {
        Set<String> listKeptDict = savedPlayList.getStringSet(KEY_PLAY_LIST, new HashSet<String>());
        if (listKeptDict.contains(name))
        {
            listKeptDict.remove(name);
            savedPlayList.edit().remove(KEY_PLAY_LIST).commit();
            savedPlayList.edit().putStringSet(KEY_PLAY_LIST, listKeptDict).commit();
            return true;
        }
        return false;
    }
//    Thread thread;
//    Runnable runnable;
    public void btnPlayClick(View view)
    {
        if (a_MainActivity.getPlayList().size() > 0)
        {
            if (isFirstTime)
            {
                Toast toast = Toast.makeText(this, R.string.message_about_start,Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP,0,0);
                toast.show();
            }

            speechIntentService = new Intent(this, z_speechService.class);
            speechIntentService.putExtra(getString(R.string.key_play_order), 0);
            speechIntentService.putExtra(getString(R.string.is_one_time), false);
            speechServiceOnPause();
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
            if (playList == null)
            {
                playList = new Intent(this, p_PlayList.class);
            }
            startActivity(playList);
        }
        progressBar.setVisibility(View.VISIBLE);

    }
    public void btnPauseClick(View view)
    {
        Toast toast = Toast.makeText(this, R.string.message_about_pause,Toast.LENGTH_SHORT);
        toast.show();
        speechServiceOnPause();

    }

    public void btnStopClick(View view)
    {
        speechServiceOnStop();
    }

    private void speechServiceOnStop()
    {
        AppData.set_Nword(0);
        AppData.set_isPause(false);

        if (speechIntentService != null)
        {
            stopService(speechIntentService);
        }
        z_speechService.stopIntentService();

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
        int order_play = -1;
        int id = view.getId();
        ArrayList<DataBaseEntry> list = new ArrayList<>();
        if (id == R.id.btn_previous)
        {
            list = getPrevious();
            order_play = 1;
        }
        if (id == R.id.btn_next)
        {
            list = getNext();
            order_play = 0;
        }
        if (list.size() > 0)
        {
            textViewEn.setText(list.get(0).get_english());
            textViewRu.setText(list.get(0).get_translate());
            textViewDict.setText(AppData.getCurrentDict());
            if (speechIntentService == null)
            {
                speechIntentService = new Intent(this, z_speechService.class);
            }
            speechIntentService.putExtra(getString(R.string.key_play_order), order_play);
            speechIntentService.putExtra(getString(R.string.is_one_time), true);
            startService(speechIntentService);
        }
    }

    private ArrayList<DataBaseEntry> getNext() throws SQLException, ExecutionException, InterruptedException
    {
        int ndict = 0; int nword = 1;
        ArrayList<DataBaseEntry> list = null;
        dataBaseQueries = new DataBaseQueries(this);
        ArrayList<String> playList = a_MainActivity.getPlayList();
        int wordsCount = dataBaseQueries.getEntriesCountAsync(playList.get(AppData.get_Ndict()));
        if (playList.size() > 0)
        {
            if (AppData.get_Ndict() == playList.size()-1 && AppData.get_Nword() == wordsCount)
            {
                if (playList.size() > 1)
                {
                    ndict = 0;
                    nword = 1;
                }
                else if(playList.size() == 1)
                {
                    ndict = AppData.get_Ndict();
                    nword = 1;
                }
            }
            else if (AppData.get_Nword() < wordsCount)
            {
                ndict = AppData.get_Ndict();
                nword = AppData.get_Nword() + 1;
            }
            else if (playList.size()>1 && AppData.get_Ndict()<playList.size()-1 && AppData.get_Nword() == wordsCount)
            {
                ndict = AppData.get_Ndict() + 1;
                nword = 1;
            }
            AppData.set_Nword(nword);
            AppData.set_Ndict(ndict);
            AppData.setCurrentDict(playList.get(ndict));
            list = dataBaseQueries.getEntriesFromDBAsync(playList.get(AppData.get_Ndict()), AppData.get_Nword(), AppData.get_Nword());
        }
        else
        {
            list.add(new DataBaseEntry(null,null,null));
        }
        return list;
    }

    private ArrayList<DataBaseEntry> getPrevious() throws SQLException, ExecutionException, InterruptedException
    {
        int ndict = 0; int nword = 1;
        ArrayList<DataBaseEntry> list = null;
        dataBaseQueries = new DataBaseQueries(this);
        ArrayList<String> playList = a_MainActivity.getPlayList();
        String dict;
        if (playList.size() > 0 && AppData.get_Ndict() < playList.size())
        {
            dict = playList.get(AppData.get_Ndict());
            int wordsCount = dataBaseQueries.getEntriesCountAsync(dict);

            if (AppData.get_Ndict() == 0 && AppData.get_Nword() == 1 && playList.size() > 1)
            {
                ndict = playList.size() - 1;
                nword = dataBaseQueries.getEntriesCountAsync(playList.get(ndict));
            }
            else if (AppData.get_Ndict() == 0 && AppData.get_Nword() == 1 && playList.size() == 1)
            {
                ndict = AppData.get_Ndict();
                nword = dataBaseQueries.getEntriesCountAsync(playList.get(ndict));
            }
            else if (AppData.get_Nword() > 1)
            {
                ndict = AppData.get_Ndict();
                nword = AppData.get_Nword() - 1;
            }
            else if (playList.size()>1 && AppData.get_Ndict()>0 && AppData.get_Nword()==1)
            {
                ndict = AppData.get_Ndict() - 1;
                nword = dataBaseQueries.getEntriesCountAsync(playList.get(ndict));
            }

            AppData.set_Nword(nword);
            AppData.set_Ndict(ndict);
            AppData.setCurrentDict(playList.get(ndict));
            list = dataBaseQueries.getEntriesFromDBAsync(playList.get(AppData.get_Ndict()), AppData.get_Nword(), AppData.get_Nword());
        }
        else
        {
            list.add(new DataBaseEntry(null,null,null));
        }
        return list;
    }

    private void speechServiceOnPause()
    {
        AppData.set_isPause(true);
        if (speechIntentService != null)
        {
            stopService(speechIntentService);
        }
        z_speechService.stopIntentService();
        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);
        textViewRu.setText(null);
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
                    settings.edit().putBoolean(KEY_ENG_ONLY,true).apply();
                    Toast toast = Toast.makeText(a_MainActivity.this,"Русскоязычное озвучивание включено",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();

                }
                else
                {
                    settings.edit().putBoolean(KEY_ENG_ONLY,false).apply();
                    Toast toast = Toast.makeText(a_MainActivity.this,"Русскоязычное озвучивание отключено",Toast.LENGTH_SHORT);
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

    public class UpdateBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String updateEN = intent.getStringExtra(z_speechService.EXTRA_KEY_UPDATE_EN);
            String updateRU = intent.getStringExtra(z_speechService.EXTRA_KEY_UPDATE_RU);
            String updateDict = intent.getStringExtra(z_speechService.EXTRA_KEY_UPDATE_DICT);
            textViewEn.setText(updateEN);
            textViewRu.setText(updateRU);
            textViewDict.setText(updateDict);
            AppData.setEnText(updateEN);
            AppData.setRuText(updateRU);
            AppData.setCurrentDict(updateDict);
            if (!textViewEn.getText().equals(null))
            {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}


