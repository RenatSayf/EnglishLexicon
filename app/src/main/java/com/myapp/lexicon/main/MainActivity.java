package com.myapp.lexicon.main;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.myapp.lexicon.addword.TranslateFragment;
import com.myapp.lexicon.cloudstorage.StorageFragment2;
import com.myapp.lexicon.database.Word;
import com.myapp.lexicon.dialogs.DictListDialog;
import com.myapp.lexicon.dialogs.OrderPlayDialog;
import com.myapp.lexicon.dialogs.RemoveDictDialog;
import com.myapp.lexicon.helpers.Share;
import com.myapp.lexicon.playlist.PlayList;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.service.LexiconService;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;
import com.myapp.lexicon.settings.SettingsFragment;
import com.myapp.lexicon.wordeditor.WordEditorActivity;
import com.myapp.lexicon.wordstests.OneOfFiveFragmNew;
import com.myapp.lexicon.wordstests.TestsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        AppData.IDictNumChangeListener
{

    public LinearLayout mainControlLayout;
    private Intent addWordIntent;
    private Intent wordEditorIntent;
    private Intent testsIntent;
    private Intent playListIntent;
    private TextView textViewEn;
    private TextView textViewRu;
    private Button btnViewDict;
    private TextView tvWordsCounter;
    private CheckBox checkBoxRuSpeak;
    private ImageView orderPlayView;
    private static Intent speechIntentService;
    public static Intent serviceIntent;
    private SpeechServiceReceiver speechServiceReceiver;
    private AppSettings appSettings;
    private AppData appData;
    private ArrayList<String> playList = new ArrayList<>();
    private Locale localeDefault;
    private ViewPager2 mainViewPager;

    private final String KEY_ENG_TEXT = "eng_text";
    private final String KEY_RU_TEXT = "ru_text";
    private final String KEY_CURRENT_DICT = "current_dict";
    private final String KEY_TV_WORDS_COUNTER = "tv_words_counter";
    private final String KEY_BTN_PLAY_VISIBLE = "btn_play_visible";
    private final String KEY_BTN_PAUSE_VISIBLE = "btn_pause_visible";
    private final String KEY_BTN_STOP_VISIBLE = "btn_stop_visible";
    private final String KEY_PROG_BAR_VISIBLE = "prog_bar_visible";

    private FragmentManager fragmentManager;

    public MainViewModel mainViewModel;
    private final CompositeDisposable composite = new CompositeDisposable();
    private Word currentWord;
    private int wordsInterval = Integer.MAX_VALUE;

    @Inject
    AlarmScheduler scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_navig_main);

        Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();
        NotificationManager nmg = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nmg != null)
        {
            nmg.cancelAll();
        }
        scheduler.cancel(AlarmScheduler.REQUEST_CODE, AlarmScheduler.REPEAT_SHOOT_ACTION);

        appSettings = new AppSettings(this);
        appData = AppData.getInstance();
        appData.initAllSettings(this);

        initViews();

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.getCurrentWord().observe(this, word -> {
            currentWord = word;
            btnViewDict.setText(currentWord.getDictName());
        });

        mainViewModel.getPlayList().observe(this, list -> {
            playList = (ArrayList<String>)list;
            if (playList.size() == 0)
            {
                speechServiceOnStop();
            }
        });

        mainViewModel.getOrderPlay().observe(this, order -> {
            if (order == 0)
            {
                mainViewModel.sortWordsList();
                orderPlayView.setImageResource(R.drawable.ic_repeat_white);
            }
            else if (order == 1)
            {
                mainViewModel.shuffleWordsList();
                orderPlayView.setImageResource(R.drawable.ic_shuffle_white);
            }
        });



        mainViewModel.getWordsList().observe(this, entries  -> {
            if (entries != null && !entries.isEmpty())
            {
                MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter(entries);
                mainViewPager.setAdapter(pagerAdapter);
                mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
                for (int i = 0; i < entries.size(); i++)
                {
                    int entriesId = entries.get(i).get_id();
                    Word currentWord = mainViewModel.getCurrentWord().getValue();
                    if (currentWord != null)
                    {
                        int currentId = currentWord.get_id();
                        if (entriesId >= currentId)
                        {
                            mainViewPager.setCurrentItem(i, false);
                            break;
                        }
                    }
                }
            }
        });

        mainViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            private int state = -1;
            private int position = -1;
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                final MainViewPagerAdapter adapter = (MainViewPagerAdapter)mainViewPager.getAdapter();
                if (adapter != null)
                {
                    Word item = adapter.getItem(position);
                    mainViewModel.setCurrentWord(item);
                    int totalWords = adapter.getItemCount();
                    String concatText = (position + 1 + "").concat(" / ").concat(totalWords + "");
                    tvWordsCounter.setText(concatText);

                }
            }
            @Override
            public void onPageScrollStateChanged(int state)
            {
                super.onPageScrollStateChanged(state);
                this.state = state;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                int remainder = position % wordsInterval;
                MainViewPagerAdapter adapter = (MainViewPagerAdapter) mainViewPager.getAdapter();
                List<Word> list = new ArrayList<>();
                if (adapter != null)
                {
                    boolean condition1 = (state == 2 && remainder == 0 && position != 0 && position > this.position);
                    boolean condition2 = (state == ViewPager2.SCROLL_STATE_DRAGGING && position == mainViewModel.wordListSize() - 1);
                    if (condition1)
                    {
                        list = adapter.getItems(position - wordsInterval, position - 1);
                    }
                    else if (condition2)
                    {
                        int lastIndex = adapter.getItemCount() - 1;
                        list = adapter.getItems(position - 1, lastIndex);
                    }
                    if ((condition1 || condition2) && list.size() > 1)
                    {
                        mainViewModel.setMainControlVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, "Проверим знания!!!...", Toast.LENGTH_LONG).show();
                        OneOfFiveFragmNew testFragment = OneOfFiveFragmNew.newInstance(list);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.from_right_to_left_anim, R.anim.from_left_to_right_anim);

                        transaction.replace(R.id.frame_to_page_fragm, testFragment).addToBackStack(null).commit();
                        mainViewPager.setCurrentItem(position - 1);
                        return;
                    }
                }
                this.position = position;
            }
        });

        mainViewModel.getMainControlVisibility().observe(this, visibility -> {
            mainControlLayout = findViewById(R.id.main_control_layout);
            if (mainControlLayout != null)
            {
                mainControlLayout.setVisibility(visibility);
            }
        });

        if (savedInstanceState == null)
        {
            BackgroundFragm backgroundFragm = new BackgroundFragm();
            getSupportFragmentManager().beginTransaction().replace(R.id.background_fragment, backgroundFragm).commit();
        }




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

        if (savedInstanceState != null)
        {
            boolean isFirstTime = false;
            tvWordsCounter.setText(savedInstanceState.getString(KEY_TV_WORDS_COUNTER));
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

    public void testPassed()
    {
        if (currentWord != null)
        {
            mainViewModel.saveCurrentWordToPref(currentWord);
            List<Word> wordList = mainViewModel.getWordsList().getValue();
            if (wordList != null)
            {
                int index = wordList.indexOf(currentWord);
                if (index <= wordList.size()-1 && index >=0)
                {
                    int i = index + 1;
                    mainViewPager.setCurrentItem(i, false);
                }
                else mainViewPager.setCurrentItem(0, false);
            }
        }
        mainViewModel.setMainControlVisibility(View.VISIBLE);
    }

    public void testFailed(int errors)
    {
        if (currentWord != null)
        {
            List<Word> wordList = mainViewModel.getWordsList().getValue();
            if (wordList != null)
            {
                int index = wordList.indexOf(currentWord) + 1;
                Integer interval = mainViewModel.getTestInterval().getValue();
                if (interval != null)
                {
                    int newIndex = index - interval;
                    if (newIndex <= wordList.size()-1 && newIndex >= 0)
                    {
                        mainViewPager.setCurrentItem(newIndex, true);
                    }
                    else mainViewPager.setCurrentItem(0, true);
                }
            }
        }
        mainViewModel.setMainControlVisibility(View.VISIBLE);
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

        mainViewPager = findViewById(R.id.mainViewPager);
        textViewEn = findViewById(R.id.enTextView);
        textViewRu = findViewById(R.id.ruTextView);
        btnViewDict = findViewById(R.id.btnViewDict);
        btnViewDictOnClick(btnViewDict);
        tvWordsCounter = findViewById(R.id.tv_words_counter);

        checkBoxRuSpeak = findViewById(R.id.check_box_ru_speak);
        checkBoxRuSpeak.setChecked(appSettings.isEnglishSpeechOnly());
        switchRuSound_OnCheckedChange();

        orderPlayView = findViewById(R.id.order_play_icon_iv);
        orderPlayViewOnClick(orderPlayView);
    }

    @SuppressWarnings("Convert2Lambda")
    private void btnViewDictOnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String buttonText = button.getText().toString();
                composite.add(mainViewModel.getDictList().subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( list -> {
                            int index = list.indexOf(buttonText);
                            String item = list.remove(index);
                            list.add(0, item);
                            DictListDialog.Companion.getInstance(list, dict -> mainViewModel.setWordsList(dict)).show(getSupportFragmentManager(), DictListDialog.Companion.getTAG());
                        }, Throwable::printStackTrace));
            }
        });
    }

    private void orderPlayViewOnClick(ImageView view)
    {
        view.setOnClickListener( v -> {
            Integer order = mainViewModel.getOrderPlay().getValue();
            if (order != null)
            {
                OrderPlayDialog.Companion.getInstance(order, ord -> mainViewModel.setOrderPlay(ord))
                        .show(getSupportFragmentManager(), OrderPlayDialog.Companion.getTAG());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TV_WORDS_COUNTER, tvWordsCounter.getText().toString());
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
        wordsInterval = mainViewModel.getTestInterval().getValue();
        appData.setDictNumberChangeListener(this);
        localeDefault = new Locale(appSettings.getTranslateLang());
        if (serviceIntent != null)
        {
            stopService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(speechServiceReceiver);
        if (AppData.getInstance().getDisplayVariant() == 1 && serviceIntent != null)
        {
            stopService(serviceIntent);
        }
        mainViewModel.saveCurrentWordToPref(currentWord);
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
        alarmClockEnable(scheduler);
    }

    private void alarmClockEnable(AlarmScheduler scheduler)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String interval = preferences.getString(getString(R.string.key_show_intervals), "0");
        if (interval != null)
        {
            int parseInt = Integer.parseInt(interval);
            if (parseInt != 0)
            {
                scheduler.scheduleRepeat((parseInt*60*1000), (parseInt*60*1000));
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
                    wordEditorIntent = new Intent(this, WordEditorActivity.class);
                }
                speechServiceOnPause();
                Bundle bundle = new Bundle();
                bundle.putString(WordEditorActivity.KEY_EXTRA_DICT_NAME, btnViewDict.getText().toString());
                String text = tvWordsCounter.getText().toString();
                try
                {
                    String[] splitArray = text.split(" ");
                    if (splitArray.length > 0)
                    {
                        bundle.putInt(WordEditorActivity.KEY_ROW_ID, Integer.parseInt(splitArray[0]));
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
            StorageFragment2 storageFragment = StorageFragment2.Companion.newInstance();
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
            TranslateFragment translateFragm = TranslateFragment.Companion.getInstance("");
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, translateFragm).addToBackStack(null).commit();
        }
//        else if (id == R.id.nav_add_dict)
//        {
//            dialogAddDict();
//        }
        else if (id == R.id.nav_delete_dict)
        {
            Disposable subscribe = mainViewModel.getDictList()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> {
                        if (list != null && !list.isEmpty())
                        {
                            RemoveDictDialog.Companion.getInstance((ArrayList<String>) list).show(getSupportFragmentManager(), RemoveDictDialog.TAG);
                        }
                    }, Throwable::printStackTrace);
            composite.add(subscribe);
        }
        else if (id == R.id.nav_edit)
        {
            if (wordEditorIntent == null)
            {
                wordEditorIntent = new Intent(this, WordEditorActivity.class);
            }
            Bundle bundle = new Bundle();
            if (playList.size() > 0)
            {
                bundle.putString(WordEditorActivity.KEY_EXTRA_DICT_NAME, playList.get(AppData.getInstance().getNdict()));
                wordEditorIntent.replaceExtras(bundle);
            }
            startActivity(wordEditorIntent);
        }
        else if (id == R.id.nav_check_your_self)
        {
            if (testsIntent == null)
            {
                testsIntent = new Intent(this, TestsActivity.class);
            }
            startActivity(testsIntent);
        }
        else if (id == R.id.nav_play_list)
        {
//            if (playListIntent == null)
//            {
//                playListIntent = new Intent(this, PlayList.class);
//            }
            playListIntent = new Intent(this, PlayList.class);
            startActivity(playListIntent);
        }
        else if (id == R.id.nav_settings)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, new SettingsFragment()).addToBackStack(null).commit();
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
            alarmClockEnable(scheduler);
            this.finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null)
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

//    private void dialogAddDict()
//    {
//        final View view = getLayoutInflater().inflate(R.layout.a_dialog_add_dict, new LinearLayout(this), false);
//        final EditText editText = view.findViewById(R.id.dialog_add_dict);
//        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
//        new AlertDialog.Builder(this).setTitle(R.string.title_new_dictionary).setIcon(R.drawable.icon_book)
//                .setPositiveButton(R.string.btn_text_add, new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//
//                        String dictName = editText.getText().toString();
//                        if (!dictName.equals(""))
//                        {
//                            try
//                            {
////                                dataBaseQueries = new DataBaseQueries(MainActivity.this);
////                                dataBaseQueries.addTableToDbSync(dictName);
//                            } catch (Exception e)
//                            {
//                                e.printStackTrace();
//                            }
//                            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.text_added_new_dict)+dictName, Toast.LENGTH_LONG);
//                            toast.setGravity(Gravity.CENTER, 0, 0);
//                            toast.show();
//                        }
//                    }
//                })
//                .setNegativeButton(R.string.btn_text_cancel, null)
//                .setView(view).create().show();
//
//        editText.addTextChangedListener(new TextWatcher()
//        {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count)
//            {
//                String str = s.toString();
//                for (int i = 0; i < str.length(); i++)
//                {
//                    int char_first = str.codePointAt(0);
//                    if ((char_first >= 33 && char_first <= 64) || (char_first >= 91 && char_first <= 96) ||
//                            (char_first >= 123 && char_first <= 126))
//                    {
//                        String str_wrong = str.substring(i);
//                        editText.setText(str.replace(str_wrong,""));
//                    }
//                    if ((str.codePointAt(i) >= 33 && str.codePointAt(i) <= 47) || (str.codePointAt(i) >= 58 && str.codePointAt(i) <= 64) ||
//                            (str.codePointAt(i) >= 91 && str.codePointAt(i) <= 96) || (str.codePointAt(i) >= 123 && str.codePointAt(i) <= 126))
//                    {
//                        String str_wrong = str.substring(i);
//                        editText.setText(str.replace(str_wrong,""));
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s){}
//        });
//    }

//    public void btnPlayClick(View view)
//    {
//        //playList = appSettings.getPlayList();
//        if (playList.size() > 0)
//        {
//            if (isFirstTime)
//            {
//                Toast toast = Toast.makeText(this, R.string.message_about_start,Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.TOP,0,0);
//                toast.show();
//            }
//
//            speechIntentService = new Intent(this, SpeechService.class);
//            stopService(speechIntentService);
//            speechIntentService.putExtra(getString(R.string.key_play_order), appSettings.getOrderPlay());
//            speechIntentService.putExtra(getString(R.string.is_one_time), false);
//            startService(speechIntentService);
//
//            //btnPlay.setVisibility(View.INVISIBLE);
//            //btnStop.setVisibility(View.VISIBLE);
//            //btnPause.setVisibility(View.VISIBLE);
//            textViewRu.setText(null);
//        } else
//        {
//            Toast toast = Toast.makeText(this, R.string.no_playlist, Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.CENTER,0,0);
//            toast.show();
//            if (playListIntent == null)
//            {
//                playListIntent = new Intent(this, PlayList.class);
//            }
//            startActivity(playListIntent);
//        }
//        progressBar.setVisibility(View.VISIBLE);
//
//    }
//    public void btnPauseClick(View view)
//    {
//        Toast toast = Toast.makeText(this, R.string.message_about_pause,Toast.LENGTH_SHORT);
//        toast.show();
//        speechServiceOnPause();
//    }

    private void speechServiceOnPause()
    {
        appData.setPause(true);
        if (speechIntentService != null)
        {
            stopService(speechIntentService);
            SpeechService.stopIntentService();
        }
        //btnPause.setVisibility(View.GONE);
        //btnPlay.setVisibility(View.VISIBLE);
        //btnStop.setVisibility(View.VISIBLE);
        //progressBar.setVisibility(View.GONE);
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
        //btnPlay.setVisibility(View.VISIBLE);
        //btnStop.setVisibility(View.GONE);
        //btnPause.setVisibility(View.GONE);
        //progressBar.setVisibility(View.GONE);

        appData.setNdict(0);
        appData.setNword(1);
    }

//    public void btnNextBackClick(View view)
//    {
//        if (playList.size() == 0)
//        {
//            Toast toast = Toast.makeText(this, R.string.no_playlist, Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.CENTER,0,0);
//            toast.show();
//            if (playListIntent == null)
//            {
//                playListIntent = new Intent(this, PlayList.class);
//            }
//            startActivity(playListIntent);
//        }
//        speechServiceOnPause();
//        if (SplashScreenActivity.speech != null)
//        {
//            SplashScreenActivity.speech.stop();
//        }
//        int id = view.getId();
//        if (id == R.id.btn_next)
//        {
//            getNext();
//        }
//
//        if (id == R.id.btn_previous)
//        {
//            getPrevious();
//        }
//    }

//    private void getNext()
//    {
//        if (playList.iterator().hasNext())
//        {
//            appData.getNextNword(this, new AppData.IGetWordListerner()
//            {
//                @Override
//                public void getWordComplete(ArrayList<DataBaseEntry> entries, Integer[] dictSize)
//                {
//                    if (entries.size() > 0)
//                    {
//                        DataBaseEntry dataBaseEntry = entries.get(0);
//                        textViewEn.setText(dataBaseEntry.getEnglish());
//                        textViewRu.setText(dataBaseEntry.getTranslate());
//                        textViewDict.setText(playList.get(appData.getNdict()));
//                        String concatText = (dataBaseEntry.getRowId() + "").concat(" / ").concat(Integer.toString(dictSize[1])).concat("  " + getString(R.string.text_studied) + " " + dictSize[2]);
//                        tvWordsCounter.setText(concatText);
//
//                        final HashMap<String, String> hashMap = new HashMap<>();
//                        hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "main_activity");
//                        try
//                        {
//                            SplashScreenActivity.speech.setLanguage(Locale.US);
//                        } catch (Exception e)
//                        {
//                            return;
//                        }
//                        SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                        {
//                            SplashScreenActivity.speech.speak(textViewEn.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
//                        } else
//                        {
//                            SplashScreenActivity.speech.speak(textViewEn.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
//                        }
//                        SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
//                        {
//                            @Override
//                            public void onStart(String utteranceId)
//                            {
//
//                            }
//
//                            @Override
//                            public void onDone(String utteranceId)
//                            {
//                                if (utteranceId.equals("main_activity") && appSettings.isEnglishSpeechOnly())
//                                {
//                                    SplashScreenActivity.speech.setLanguage(localeDefault);
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                                    {
//                                        SplashScreenActivity.speech.speak(textViewRu.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
//                                    } else
//                                    {
//                                        SplashScreenActivity.speech.speak(textViewRu.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
//                                    }
//                                }
//                                SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
//                            }
//
//                            @Override
//                            public void onError(String utteranceId)
//                            {
//
//                            }
//                        });
//                    }
//                }
//            });
//        }
//    }
//
//    private void getPrevious()
//    {
//        if (playList.size() == 0)
//        {
//            speechServiceOnStop();
//            return;
//        }
//
//        appData.getPreviousNword(this, new AppData.IGetWordListerner()
//        {
//            @Override
//            public void getWordComplete(ArrayList<DataBaseEntry> entries, Integer[] dictSize)
//            {
//                if (entries.size() > 0)
//                {
//                    DataBaseEntry dataBaseEntry = entries.get(0);
//
//                    textViewEn.setText(dataBaseEntry.getEnglish());
//                    textViewRu.setText(dataBaseEntry.getTranslate());
//                    textViewDict.setText(playList.get(appData.getNdict()));
//                    String concatText = (dataBaseEntry.getRowId() + "").concat(" / ").concat(Integer.toString(dictSize[1])).concat("  " + getString(R.string.text_studied) + " " + dictSize[2]);
//                    tvWordsCounter.setText(concatText);
//
//                    final HashMap<String, String> hashMap = new HashMap<>();
//                    hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "main_activity");
//                    try
//                    {
//                        SplashScreenActivity.speech.setLanguage(Locale.US);
//                    } catch (Exception e)
//                    {
//                        return;
//                    }
//                    SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                    {
//                        SplashScreenActivity.speech.speak(textViewEn.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
//                    } else
//                    {
//                        SplashScreenActivity.speech.speak(textViewEn.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
//                    }
//                    SplashScreenActivity.speech.setOnUtteranceProgressListener(new UtteranceProgressListener()
//                    {
//                        @Override
//                        public void onStart(String utteranceId)
//                        {
//
//                        }
//
//                        @Override
//                        public void onDone(String utteranceId)
//                        {
//                            if (utteranceId.equals("main_activity") && appSettings.isEnglishSpeechOnly())
//                            {
//                                SplashScreenActivity.speech.setLanguage(localeDefault);
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//                                {
//                                    SplashScreenActivity.speech.speak(textViewRu.getText().toString(), TextToSpeech.QUEUE_ADD, null, hashMap.get(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID));
//                                } else
//                                {
//                                    SplashScreenActivity.speech.speak(textViewRu.getText().toString(), TextToSpeech.QUEUE_ADD, hashMap);
//                                }
//                            }
//                            SplashScreenActivity.speech.setOnUtteranceProgressListener(null);
//                        }
//
//                        @Override
//                        public void onError(String utteranceId)
//                        {
//
//                        }
//                    });
//                }
//            }
//        });
//    }

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

    public static Boolean isActivityRunning = false;

    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        isActivityRunning = true;
    }

    @Override
    public void onDetachedFromWindow()
    {
        super.onDetachedFromWindow();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isUseService = preferences.getBoolean("service", true);
        ArrayList<String> playList = appSettings.getPlayList();
        if (playList.size() > 0 && isUseService)
        {
            if (serviceIntent == null)
            {
                serviceIntent = new Intent(this, LexiconService.class);
            }
            startService(serviceIntent);
        }
        isActivityRunning = false;
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

    public void testIntervalOnChange(int value)
    {
        wordsInterval = value;
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
            btnViewDict.setText(updateDict);
            tvWordsCounter.setText(nword);
//            if (!textViewEn.getText().equals(""))
//            {
//                progressBar.setVisibility(View.GONE);
//            }
        }
    }

}


