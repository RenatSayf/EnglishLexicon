package com.myapp.lexicon.main;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.myapp.lexicon.addword.TranslateFragment;
import com.myapp.lexicon.cloudstorage.StorageFragment2;
import com.myapp.lexicon.database.Word;
import com.myapp.lexicon.dialogs.DictListDialog;
import com.myapp.lexicon.dialogs.OrderPlayDialog;
import com.myapp.lexicon.dialogs.RemoveDictDialog;
import com.myapp.lexicon.helpers.NetHelper;
import com.myapp.lexicon.helpers.Share;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.service.LexiconService;
import com.myapp.lexicon.settings.SettingsFragment;
import com.myapp.lexicon.wordeditor.WordEditorActivity;
import com.myapp.lexicon.wordstests.OneOfFiveFragmNew;
import com.myapp.lexicon.wordstests.TestsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    public LinearLayout mainControlLayout;
    private Intent testsIntent;
    private Button btnViewDict;
    private TextView tvWordsCounter;
    private ImageView orderPlayView;
    private ViewPager2 mainViewPager;

    private final String KEY_TV_WORDS_COUNTER = "tv_words_counter";

    public MainViewModel mainViewModel;
    private SpeechViewModel speechViewModel;
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

        NotificationManager nmg = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nmg != null)
        {
            nmg.cancelAll();
        }
        scheduler.cancel(AlarmScheduler.REQUEST_CODE, AlarmScheduler.REPEAT_SHOOT_ACTION);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        speechViewModel = new ViewModelProvider(this).get(SpeechViewModel.class);


        btnViewDict = findViewById(R.id.btnViewDict);
        btnViewDictOnClick(btnViewDict);
        mainViewModel.getCurrentWord().observe(this, word -> {
            currentWord = word;
            btnViewDict.setText(currentWord.getDictName());
        });


        orderPlayView = findViewById(R.id.order_play_icon_iv);
        orderPlayViewOnClick(orderPlayView);
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

        mainViewPager = findViewById(R.id.mainViewPager);
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

        tvWordsCounter = findViewById(R.id.tv_words_counter);

        mainViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            private int state = -1;
            private int position = -1;
            private Word word;

            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                final MainViewPagerAdapter adapter = (MainViewPagerAdapter)mainViewPager.getAdapter();
                if (adapter != null)
                {
                    Word item = adapter.getItem(position);
                    word = item;
                    mainViewModel.setCurrentWord(item);
                    int totalWords = adapter.getItemCount();
                    String concatText = (position + 1) + " / " + totalWords;
                    tvWordsCounter.setText(concatText);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state)
            {
                super.onPageScrollStateChanged(state);
                this.state = state;
                if (speechViewModel.isEnSpeech().getValue() != null)
                {
                    Boolean isEnSpeech = speechViewModel.isEnSpeech().getValue();
                    if (state == 1 && isEnSpeech != null && isEnSpeech)
                    {
                        speechViewModel.setSpeechProgressVisibility(View.VISIBLE);
                    }
                    Integer mControlVisibility = mainViewModel.getMainControlVisibility().getValue();
                    if (isEnSpeech != null && isEnSpeech && word != null && state == 0 && mControlVisibility != null && mControlVisibility == View.VISIBLE)
                    {
                        speechViewModel.doSpeech(word.getEnglish(), Locale.US);
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                int remainder = position % wordsInterval;
                int testIntervalIndex = position - remainder;
                mainViewModel.setIntermediateIndex(testIntervalIndex);
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
                        int firstIndex = -1;
                        if (mainViewModel.getIntermediateIndex().getValue() != null)
                        {
                            firstIndex = mainViewModel.getIntermediateIndex().getValue();
                        }
                        if (firstIndex < 0) firstIndex = 0;
                        list = adapter.getItems(firstIndex, lastIndex);
                    }
                    if ((condition1 || condition2) && list.size() > 1)
                    {
                        speechViewModel.stopSpeech();
                        mainViewModel.setIntermediateIndex(position);
                        mainViewModel.setMainControlVisibility(View.INVISIBLE);
                        speechViewModel.setSpeechProgressVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this, getString(R.string.text_test_knowledge), Toast.LENGTH_LONG).show();
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
            tvWordsCounter.setText(savedInstanceState.getString(KEY_TV_WORDS_COUNTER));
        }

        if (NetHelper.INSTANCE.isOnline(this))
        {
            if (savedInstanceState == null)
            {
                MobileAds.initialize(this, getString(R.string.admob_app_id));
                MainBannerFragment bannerFragment = new MainBannerFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.banner_frame_main, bannerFragment).commit();
            }
        }

        CheckBox checkBoxEnView = findViewById(R.id.check_box_en_speak);
        //noinspection CodeBlock2Expr
        checkBoxEnView.setOnCheckedChangeListener((compoundButton, b) -> {
            speechViewModel.setEnSpeech(b);
        });
        speechViewModel.isEnSpeech().observe(this, checked -> {
            checkBoxEnView.setChecked(checked);
            if (!checked)
            {
                speechViewModel.setSpeechProgressVisibility(View.INVISIBLE);
            }
        });
        speechViewModel.getEnCheckboxEnable().observe(this, checkBoxEnView::setEnabled);

        CheckBox checkBoxRuSpeak = findViewById(R.id.check_box_ru_speak);
        checkBoxRuSpeak.setOnCheckedChangeListener((compoundButton, b) -> {
            speechViewModel.setRuSpeech(b);
            if (b)
                Toast.makeText(MainActivity.this, R.string.text_ru_speech_on, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, R.string.text_ru_speech_off, Toast.LENGTH_SHORT).show();
        });

        speechViewModel.isRuSpeech().observe(this, checked -> {
            //noinspection Convert2MethodRef
            checkBoxRuSpeak.setChecked(checked);
        });
        speechViewModel.getRuCheckboxEnable().observe(this, checkBoxRuSpeak::setEnabled);


        ProgressBar speechProgress = findViewById(R.id.speechProgress);
        //noinspection CodeBlock2Expr
        speechViewModel.getSpeechStartId().observe(MainActivity.this, utteranceId ->
        {
            speechViewModel.setSpeechProgressVisibility(View.INVISIBLE);
        });

        speechViewModel.getSpeechDoneId().observe(MainActivity.this, utteranceId ->
        {
            speechViewModel.setSpeechProgressVisibility(View.INVISIBLE);
            Boolean isRu = speechViewModel.isRuSpeech().getValue();
            Word word = mainViewModel.getCurrentWord().getValue();
            if (utteranceId.equals("En") && isRu != null && isRu && word != null)
            {
                speechViewModel.doSpeech(word.getTranslate(), Locale.getDefault());
            }
        });

        //noinspection CodeBlock2Expr
        speechViewModel.getSpeechError().observe(this, err -> {
            speechViewModel.setSpeechProgressVisibility(View.INVISIBLE);
        });


        ImageButton btnSpeak = findViewById(R.id.btn_speak);
        speechViewModel.getSpeechProgressVisibility().observe(this, v -> {
            speechProgress.setVisibility(v);
            if (v == View.VISIBLE)
                btnSpeak.setVisibility(View.INVISIBLE);
            else btnSpeak.setVisibility(View.VISIBLE);
        });


        btnSpeak.setOnClickListener(view -> {
            int position = mainViewPager.getCurrentItem();
            Boolean isEnSpeech = speechViewModel.isEnSpeech().getValue();
            Boolean isRuSpeech = speechViewModel.isRuSpeech().getValue();
            if (mainViewModel.getWordsList().getValue() != null)
            {
                Word word = mainViewModel.getWordsList().getValue().get(position);
                String enText = word.getEnglish();
                String ruText = word.getTranslate();
                if (isEnSpeech != null && isEnSpeech)
                {
                    speechViewModel.doSpeech(enText, Locale.US);
                    speechViewModel.setSpeechProgressVisibility(View.VISIBLE);
                }
                if (isRuSpeech != null && isRuSpeech)
                {
                    Locale localeRu = new Locale(getString(R.string.lang_code_translate));
                    speechViewModel.doSpeech(ruText, localeRu);
                }
            }
        });

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
        if (currentWord != null && errors > 0)
        {
            List<Word> wordList = mainViewModel.getWordsList().getValue();
            if (wordList != null && mainViewModel.getIntermediateIndex().getValue() != null)
            {
                int newIndex = mainViewModel.getIntermediateIndex().getValue();
                if (newIndex <= wordList.size()-1 && newIndex >= 0)
                {
                    mainViewPager.setCurrentItem(newIndex, true);
                }
                else mainViewPager.setCurrentItem(0, true);
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
                            if (index >= 0)
                            {
                                String item = list.remove(index);
                                list.add(0, item);
                            }
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
    protected void onStop()
    {
        FirebaseUserActions.getInstance().end(getAction());
        super.onStop();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mainViewModel.getTestInterval().getValue() != null)
        {
            wordsInterval = mainViewModel.getTestInterval().getValue();
        }
        this.stopService(new Intent(this, LexiconService.class));
    }

    @Override
    protected void onDestroy()
    {
        mainViewModel.saveCurrentWordToPref(currentWord);
        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer != null)
        {
            if (drawer.isDrawerOpen(GravityCompat.START))
            {
                drawer.closeDrawer(GravityCompat.START);
            }
        }
        alarmClockEnable(scheduler);
        super.onBackPressed();
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
            Intent intent = new Intent(this, WordEditorActivity.class);
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
            intent.replaceExtras(bundle);
            startActivity(intent);
        }
        if (id == R.id.edit_speech_data)
        {
            Intent speechEditorIntent = new Intent(Intent.ACTION_VIEW);
            speechEditorIntent.setAction(Settings.ACTION_SETTINGS);
            startActivity(speechEditorIntent);
        }

        if (id == R.id.cloud_storage)
        {
            StorageFragment2 storageFragment = StorageFragment2.Companion.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, storageFragment).addToBackStack(null).commit();
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
        if (id == R.id.nav_add_word)
        {
            TranslateFragment translateFragm = TranslateFragment.Companion.getInstance("");
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, translateFragm).addToBackStack(null).commit();
        }
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
            Intent intent = new Intent(this, WordEditorActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(WordEditorActivity.KEY_EXTRA_DICT_NAME, btnViewDict.getText().toString());
            intent.replaceExtras(bundle);
            startActivity(intent);
        }
        else if (id == R.id.nav_check_your_self)
        {
            if (testsIntent == null)
            {
                testsIntent = new Intent(this, TestsActivity.class);
            }
            startActivity(testsIntent);
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
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, aboutAppFragment).addToBackStack(null).commit();
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
        if (isUseService)
        {
            Intent serviceIntent = new Intent(this, LexiconService.class);
            startService(serviceIntent);
        }
        isActivityRunning = false;
    }

    public void testIntervalOnChange(int value)
    {
        wordsInterval = value;
    }




}


