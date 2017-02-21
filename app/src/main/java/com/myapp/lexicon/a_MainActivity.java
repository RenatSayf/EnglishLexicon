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
    public static SharedPreferences kept_playList;
    public static SharedPreferences settings;
    public static String KEY_ENG_ONLY = "eng_only";
    public static DatabaseHelper databaseHelper;

    private Intent add_word;
    private Intent _word_editor;
    private Intent _check_self;
    private Intent _play_list;
    private TextView _textViewEn;
    private TextView _textViewRu;
    private TextView _textViewDict;
    private Button _btn_Play;
    private Button _btn_Stop;
    private Button _btn_Pause;
    private Button _btn_Next;
    private Button _btn_Previous;
    private ProgressBar _progressBar;
    private Switch switchRuSound;
    private Intent speechIntentService;
    private UpdateBroadcastReceiver mUpdateBroadcastReceiver;
    private z_BackgroundAnim backgroundAnim;
    private boolean isFirstTime = true;

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
        kept_playList=getSharedPreferences("play_list", MODE_PRIVATE);
        settings = getSharedPreferences(KEY_ENG_ONLY, MODE_PRIVATE);
        initViews();

        speechIntentService = new Intent(this, z_speechService.class);
        //speechIntentService = new Intent(this, z_speechService2.class);

        // Регистрируем приёмник
        mUpdateBroadcastReceiver = new UpdateBroadcastReceiver();
        IntentFilter updateIntentFilter = new IntentFilter(z_speechService.ACTION_UPDATE);
        //IntentFilter updateIntentFilter = new IntentFilter(z_speechService2.ACTION_UPDATE);
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

        if (databaseHelper == null)
        {
            databaseHelper = new DatabaseHelper(this);
            databaseHelper.create_db();
        }

        if (savedInstanceState != null)
        {
            isFirstTime = false;
            _textViewDict.setVisibility(View.VISIBLE);
        }

    }
    private void initViews()
    {
        backgroundAnim = new z_BackgroundAnim(this, (ViewFlipper) findViewById(R.id.view_flipper));
        backgroundAnim.startAnimByRandom();
        _textViewEn = (TextView) findViewById(R.id.enTextView);
        _textViewRu = (TextView) findViewById(R.id.ruTextView);
        _textViewDict = (TextView) findViewById(R.id.textViewDict);
        _textViewDict.setVisibility(View.INVISIBLE);
        _btn_Play = (Button) findViewById(R.id.btn_play);
        _btn_Stop = (Button) findViewById(R.id.btn_stop);
        _btn_Pause = (Button) findViewById(R.id.btn_pause);
        _btn_Previous = (Button) findViewById(R.id.btn_previous);
        _btn_Next = (Button) findViewById(R.id.btn_next);
        _progressBar = (ProgressBar) findViewById(R.id.progressBar);
        switchRuSound = (Switch) findViewById(R.id.switch_ru_sound);
        switchRuSound.setChecked(settings.getBoolean(KEY_ENG_ONLY, true));
        switchRuSound_OnCheckedChange();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        AppData.setEnText(_textViewEn.getText().toString());
        AppData.setRuText(_textViewRu.getText().toString());
        AppData.setCurrentDict(_textViewDict.getText().toString());
        AppData.buttonPlayVisible = _btn_Play.getVisibility();
        AppData.buttonStopVisible = _btn_Stop.getVisibility();
        AppData.buttonPauseVisible = _btn_Pause.getVisibility();
        AppData.buttonNextVisible = _btn_Next.getVisibility();
        AppData.buttonPreviousVisible = _btn_Previous.getVisibility();
        AppData.progBarMainActVisible = _progressBar.getVisibility();
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
        try
        {
            if (!a_MainActivity.databaseHelper.database.isOpen())
            {
                a_MainActivity.databaseHelper.open();
            }
        }
        catch (Exception ex)
        {
            z_Log.v(ex.getMessage());
        }
        _textViewEn.setText(AppData.getEnText());
        _textViewRu.setText(AppData.getRuText());
        _textViewDict.setText(AppData.getCurrentDict());
        _btn_Play.setVisibility(AppData.buttonPlayVisible);
        _btn_Stop.setVisibility(AppData.buttonStopVisible);
        _btn_Pause.setVisibility(AppData.buttonPauseVisible);
        _btn_Next.setVisibility(AppData.buttonNextVisible);
        _btn_Previous.setVisibility(AppData.buttonPreviousVisible);
        _progressBar.setVisibility(AppData.progBarMainActVisible);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        databaseHelper.close();
        unregisterReceiver(mUpdateBroadcastReceiver);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        if (!isActivityOnTop())
        {

        }
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
            if (add_word == null)
            {
                add_word = new Intent(this,b_AddWordActivity.class);
            }
            startActivity(add_word);
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
            if (_word_editor == null)
            {
                _word_editor = new Intent(this, d_WordEditor.class);
            }
            startActivity(_word_editor);
        }
        else if (id == R.id.nav_check_your_self)
        {
            if (_check_self == null)
            {
                _check_self = new Intent(this, t_Tests.class);
            }
            speechServiceOnPause();
            startActivity(_check_self);
        } else if (id == R.id.nav_play_list)
        {
            if (_play_list == null)
            {
                _play_list = new Intent(this, p_PlayList.class);
            }
            startActivity(_play_list);
        } else if (id == R.id.nav_exit)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            backgroundAnim.onDestroy();
            databaseHelper.close();
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
    public static void savePlayList(ArrayList<p_ItemListDict> addDicts)
    {
        Set<String> existDicts = a_MainActivity.kept_playList.getStringSet("play_list", new HashSet<String>());
        for (int i = 0; i < addDicts.size(); i++)
        {
            existDicts.add(addDicts.get(i).get_dictName());
        }
        kept_playList.edit().remove("play_list").commit();
        kept_playList.edit().putStringSet("play_list", existDicts).commit();
    }
    public static ArrayList<p_ItemListDict> getPlayList()
    {
        ArrayList<p_ItemListDict> listDicts=new ArrayList<>();
        Set<String> listKeptDict = kept_playList.getStringSet("play_list", new HashSet<String>());
        for (String item : listKeptDict)
        {
            listDicts.add(new p_ItemListDict(item, true));
        }

        return listDicts;
    }
    public static boolean removeItemPlayList(String name)
    {
        Set<String> listKeptDict = kept_playList.getStringSet("play_list", new HashSet<String>());
        if (listKeptDict.contains(name))
        {
            listKeptDict.remove(name);
            kept_playList.edit().remove("play_list").commit();
            kept_playList.edit().putStringSet("play_list", listKeptDict).commit();
            return true;
        }
        return false;
    }
    Thread thread;
    Runnable runnable;
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
            //speechIntentService = new Intent(this, z_speechService.class);
            speechIntentService.putExtra(getString(R.string.key_play_order), 0);
            speechIntentService.putExtra(getString(R.string.is_one_time), false);
            startService(speechIntentService);
            _btn_Play.setVisibility(View.GONE);
            _btn_Stop.setVisibility(View.VISIBLE);
            _btn_Pause.setVisibility(View.VISIBLE);
            _btn_Next.setVisibility(View.VISIBLE);
            _btn_Previous.setVisibility(View.VISIBLE);
            _textViewRu.setText(null);
            _textViewDict.setVisibility(View.VISIBLE);
        } else
        {
            Toast toast = Toast.makeText(this, R.string.no_playlist, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
            if (_play_list == null)
            {
                _play_list = new Intent(this, p_PlayList.class);
            }
            startActivity(_play_list);
        }
        _progressBar.setVisibility(View.VISIBLE);

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
        else
        {
            stopService(new Intent(this, z_speechService.class));
        }
//        z_speechService.stopIntentService();
//        z_speechService.resetCount();
        _textViewEn.setText(null);
        _textViewRu.setText(null);
        _btn_Play.setVisibility(View.VISIBLE);
        _btn_Stop.setVisibility(View.GONE);
        _btn_Pause.setVisibility(View.GONE);
        _btn_Next.setVisibility(View.GONE);
        _btn_Previous.setVisibility(View.GONE);
        _textViewDict.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        backgroundAnim.onSaveInstanceState(null);
//        outState.putString("enText", _textViewEn.getText().toString());
//        outState.putString("ruText", _textViewRu.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
//        _textViewEn.setText(savedInstanceState.getString("enText"));
//        _textViewRu.setText(savedInstanceState.getString("ruText"));
    }

    private DataBaseQueries dataBaseQueries;
    public void btnBackClick(View view) throws SQLException, ExecutionException, InterruptedException
    {
        speechServiceOnPause();
        ArrayList<DataBaseEntry> list = getBack();
        if (list.size() > 0)
        {
            _textViewEn.setText(list.get(0).get_english());
            _textViewRu.setText(list.get(0).get_translate());
            _textViewDict.setText(AppData.getCurrentDict());
        }
    }
    public void btnNextClick(View view) throws SQLException, ExecutionException, InterruptedException
    {
        speechServiceOnPause();
        ArrayList<DataBaseEntry> list = getNext();
        if (list.size() > 0)
        {
            _textViewEn.setText(list.get(0).get_english());
            _textViewRu.setText(list.get(0).get_translate());
            _textViewDict.setText(AppData.getCurrentDict());
        }
    }

    private ArrayList<DataBaseEntry> getNext() throws SQLException, ExecutionException, InterruptedException
    {
        int ndict = 0; int nword = 1;
        ArrayList<DataBaseEntry> list = null;
        dataBaseQueries = new DataBaseQueries(this);
        ArrayList<p_ItemListDict> playList = a_MainActivity.getPlayList();
        int wordsCount = dataBaseQueries.getEntriesCountAsync(playList.get(AppData.get_Ndict()).get_dictName());
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
            else if (playList.size()>1 && AppData.get_Ndict()<playList.size()-1 && AppData.get_Nword()==wordsCount)
            {
                ndict = AppData.get_Ndict() + 1;
                nword = 1;
            }
            AppData.set_Nword(nword);
            AppData.set_Ndict(ndict);
            AppData.setCurrentDict(playList.get(ndict).get_dictName());
            list = dataBaseQueries.getEntriesFromDBAsync(playList.get(AppData.get_Ndict()).get_dictName(), AppData.get_Nword(), AppData.get_Nword());
        }
        else
        {
            list.add(new DataBaseEntry(null,null,null));
        }
        return list;
    }

    private ArrayList<DataBaseEntry> getBack() throws SQLException, ExecutionException, InterruptedException
    {
        int ndict = 0; int nword = 1;
        ArrayList<DataBaseEntry> list = null;
        dataBaseQueries = new DataBaseQueries(this);
        ArrayList<p_ItemListDict> playList = a_MainActivity.getPlayList();
        p_ItemListDict dict;
        if (playList.size() > 0 && AppData.get_Ndict() < playList.size())
        {
            dict = playList.get(AppData.get_Ndict());
            int wordsCount = dataBaseQueries.getEntriesCountAsync(dict.get_dictName());

            if (AppData.get_Ndict() == 0 && AppData.get_Nword() == 1 && playList.size() > 1) // 1
            {
                //z_Log.v(" Вариант 1   AppData.get_Ndict() = " + AppData.get_Ndict() + "    AppData.get_Nword() = " + AppData.get_Nword() + "   playList.size() = " + playList.size());
                ndict = playList.size() - 1;
                nword = dataBaseQueries.getEntriesCountAsync(playList.get(ndict).get_dictName());
            }
            else if (AppData.get_Ndict() == 0 && AppData.get_Nword() == 1 && playList.size() == 1) // 2
            {
                //z_Log.v(" Вариант 2   AppData.get_Ndict() = " + AppData.get_Ndict() + "    AppData.get_Nword() = " + AppData.get_Nword());
                ndict = AppData.get_Ndict();
                nword = dataBaseQueries.getEntriesCountAsync(playList.get(ndict).get_dictName());
            }
            else if (AppData.get_Nword() > 1) // 3
            {
                //z_Log.v(" Вариант 3   AppData.get_Ndict() = " + AppData.get_Ndict() + "    AppData.get_Nword() = " + AppData.get_Nword() + "   playList.size()-1 = " + (playList.size()-1));
                ndict = AppData.get_Ndict();
                nword = AppData.get_Nword() - 1;
            }
            else if (playList.size()>1 && AppData.get_Ndict()>0 && AppData.get_Nword()==1) // 4
            {
                //z_Log.v(" Вариант 3   AppData.get_Ndict() = " + AppData.get_Ndict() + "    AppData.get_Nword() = " + AppData.get_Nword() + "   playList.size() = " + playList.size());
                ndict = AppData.get_Ndict() - 1;
                nword = dataBaseQueries.getEntriesCountAsync(playList.get(ndict).get_dictName());
            }

            AppData.set_Nword(nword);
            AppData.set_Ndict(ndict);
            AppData.setCurrentDict(playList.get(ndict).get_dictName());
            list = dataBaseQueries.getEntriesFromDBAsync(playList.get(AppData.get_Ndict()).get_dictName(), AppData.get_Nword(), AppData.get_Nword());
            //z_Log.v("list.size() = " + list.size() + "   AppData.get_Ndict() = " + AppData.get_Ndict() + "  AppData.get_Nword() = " + AppData.get_Nword());
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
        else
        {
            stopService(new Intent(this, z_speechService.class));
        }
        //z_speechService.stopIntentService();
        _btn_Pause.setVisibility(View.GONE);
        _btn_Play.setVisibility(View.VISIBLE);
        _textViewRu.setText(null);
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
            _textViewEn.setText(updateEN);
            _textViewRu.setText(updateRU);
            _textViewDict.setText(updateDict);
            AppData.setEnText(updateEN);
            AppData.setRuText(updateRU);
            AppData.setCurrentDict(updateDict);
            if (!_textViewEn.getText().equals(null))
            {
                _progressBar.setVisibility(View.GONE);
            }
        }
    }
}


