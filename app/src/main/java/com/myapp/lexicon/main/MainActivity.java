package com.myapp.lexicon.main;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
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
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.aboutapp.AboutAppFragment;
import com.myapp.lexicon.addword.TranslateFragment;
import com.myapp.lexicon.ads.AdViewModel2;
import com.myapp.lexicon.billing.BillingViewModel;
import com.myapp.lexicon.cloudstorage.StorageFragment2;
import com.myapp.lexicon.database.AppDB;
import com.myapp.lexicon.database.AppDao;
import com.myapp.lexicon.database.AppDataBase;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.dialogs.DictListDialog;
import com.myapp.lexicon.dialogs.OrderPlayDialog;
import com.myapp.lexicon.dialogs.RemoveDictDialog;
import com.myapp.lexicon.helpers.AppBus;
import com.myapp.lexicon.helpers.Share;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.service.LexiconService;
import com.myapp.lexicon.settings.SettingsFragment;
import com.myapp.lexicon.wordeditor.WordEditorActivity;
import com.myapp.lexicon.wordstests.OneOfFiveFragm;
import com.myapp.lexicon.wordstests.TestFragment;
import com.yandex.mobile.ads.banner.AdSize;
import com.yandex.mobile.ads.banner.BannerAdEventListener;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequestError;
import com.yandex.mobile.ads.common.ImpressionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
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
    private Button btnViewDict;
    private TextView tvWordsCounter;
    private ImageView orderPlayView;
    private ViewPager2 mainViewPager;

    private final String KEY_TV_WORDS_COUNTER = "tv_words_counter";

    public MainViewModel mainViewModel;
    private SpeechViewModel speechViewModel;
    private AdViewModel2 adsVM;
    private final CompositeDisposable composite = new CompositeDisposable();
    private Word currentWord;
    private int wordsInterval = Integer.MAX_VALUE;

    @Inject
    AlarmScheduler scheduler;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        View root = LayoutInflater.from(this).inflate(R.layout.a_navig_main, new DrawerLayout(this));
        setContentView(root);

        Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this);

        NotificationManager nmg = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nmg != null)
        {
            nmg.cancelAll();
        }
        scheduler.cancel(AlarmScheduler.REQUEST_CODE, AlarmScheduler.REPEAT_SHOOT_ACTION);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        speechViewModel = new ViewModelProvider(this).get(SpeechViewModel.class);
        BillingViewModel billingVM = new ViewModelProvider(this).get(BillingViewModel.class);
        adsVM = new ViewModelProvider(this).get(AdViewModel2.class);

        billingVM.getNoAdsToken().observe(this, t -> {
            if (t == null)
            {
                BannerAdView adBanner = findViewById(R.id.banner_main);
                if (adBanner != null)
                {
                    String adId = adsVM.getBannerAdId(0);
                    adBanner.setAdUnitId(adId);
                    adBanner.setAdSize(AdSize.stickySize(AdSize.FULL_SCREEN.getWidth()));
                    adBanner.setBannerAdEventListener(new BannerAdEventListener()
                    {
                        @Override
                        public void onAdLoaded()
                        {
                            if (BuildConfig.DEBUG)
                            {
                                System.out.println("************* Banner is loaded ******************");
                            }
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull AdRequestError adRequestError)
                        {
                            if (BuildConfig.DEBUG)
                            {
                                System.out.println("**************** Banner Error" + adRequestError.getDescription() + " *******************");
                            }
                        }

                        @Override
                        public void onAdClicked()
                        {}

                        @Override
                        public void onLeftApplication()
                        {}

                        @Override
                        public void onReturnedToApplication()
                        {}

                        @Override
                        public void onImpression(@Nullable ImpressionData impressionData)
                        {}
                    });
                    adBanner.loadAd(new com.yandex.mobile.ads.common.AdRequest.Builder().build());
                }
            }
        });

        btnViewDict = findViewById(R.id.btnViewDict);
        btnViewDictOnClick(btnViewDict);
        mainViewModel.getCurrentWord().observe(this, word -> {
            currentWord = word;
            btnViewDict.setText(currentWord.getDictName());
        });

        mainViewModel.currentDict.observe(this, dict -> {
            if (!dict.isEmpty())
            {
                btnViewDict.setText(dict);
            } else
            {
                btnViewDict.setText(getString(R.string.text_dictionary));
            }
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
        mainViewModel.wordsList.observe(this, entries  -> {
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
                if (position < mainViewModel.wordListSize() - 1)
                {
                    mainViewModel.wordsIsEnded(false);
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
                        if (currentWord != null && !currentWord.getEnglish().equals(""))
                        {
                            speechViewModel.setSpeechProgressVisibility(View.VISIBLE);
                        }
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
                        OneOfFiveFragm testFragment = OneOfFiveFragm.newInstance(list);
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
        checkBoxRuSpeak.setOnClickListener( view -> {
            CheckBox checkBox = (CheckBox) view;
            speechViewModel.setRuSpeech(checkBox.isChecked());
            if (checkBox.isChecked())
            {
                Toast.makeText(MainActivity.this, R.string.text_ru_speech_on, Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(MainActivity.this, R.string.text_ru_speech_off, Toast.LENGTH_SHORT).show();
            }
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
            if (mainViewModel.wordsList.getValue() != null)
            {
                Word word = mainViewModel.wordsList.getValue().get(position);
                String enText = word.getEnglish();
                if (isEnSpeech != null)
                {
                    speechViewModel.doSpeech(enText, Locale.US);
                    speechViewModel.setSpeechProgressVisibility(View.VISIBLE);
                }

            }
        });

        AppCompatImageButton btnReplay = findViewById(R.id.btnReplay);
        mainViewModel.isEndWordList.observe(this, isEnd -> {
            if (isEnd)
            {
                btnReplay.setVisibility(View.VISIBLE);
            }
            else btnReplay.setVisibility(View.INVISIBLE);
        });

        btnReplay.setOnClickListener( view -> mainViewPager.setCurrentItem(0, true));

    }

    public void testPassed()
    {
        if (currentWord != null)
        {
            mainViewModel.saveCurrentWordToPref(currentWord);
            List<Word> wordList = mainViewModel.wordsList.getValue();
            if (wordList != null)
            {
                int index = wordList.indexOf(currentWord);
                if (index <= wordList.size()-1 && index >=0)
                {
                    int i = index + 1;
                    mainViewPager.setCurrentItem(i, false);
                }
                else mainViewPager.setCurrentItem(0, false);
                mainViewModel.wordsIsEnded(index + 1 == mainViewModel.wordListSize() - 1);
            }
        }
        mainViewModel.setMainControlVisibility(View.VISIBLE);
    }

    public void testFailed(int errors)
    {
        if (currentWord != null && errors > 0)
        {
            List<Word> wordList = mainViewModel.wordsList.getValue();
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
        FirebaseAppIndex.getInstance(this).update();
        FirebaseUserActions.getInstance(this).start(getAction());
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
                            DictListDialog.Companion.getInstance(list, new DictListDialog.ISelectItemListener()
                            {
                                @Override
                                public void dictListItemOnSelected(@NonNull String dict)
                                {
                                    mainViewModel.setWordsList(dict, 1);
                                    Word word = new Word(1, dict, "", "", 1);
                                    mainViewModel.saveCurrentWordToPref(word);
                                    mainViewModel.setCurrentWord(word);
                                    mainViewModel.setCurrentDict(dict);
                                }
                            }).show(getSupportFragmentManager(), DictListDialog.Companion.getTAG());

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
        FirebaseUserActions.getInstance(this).end(getAction());
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
        Boolean isRefresh = AppBus.INSTANCE.isRefresh().getValue();
        if (isRefresh != null && isRefresh)
        {
            mainViewModel.refreshWordsList();
        }

        this.getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
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
                finish();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        mainViewModel.saveCurrentWordToPref(currentWord);
        super.onDestroy();
    }

    private void alarmClockEnable(AlarmScheduler scheduler)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String interval = preferences.getString(getString(R.string.key_show_intervals), "0");
        int parseInt = Integer.parseInt(interval);
        if (parseInt != 0)
        {
            scheduler.scheduleOne((long) parseInt * 60 * 1000);
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
            Word word = mainViewModel.getCurrentWord().getValue();
            if (word != null)
            {
                AppBus.INSTANCE.passWord(word); // отправляем слово в WordEditorActivity
            }
            Intent intent = new Intent(this, WordEditorActivity.class);
            startActivity(intent);
        }
        if (id == R.id.edit_speech_data)
        {
            try
            {
                Intent speechEditorIntent = new Intent(Intent.ACTION_VIEW);
                speechEditorIntent.setAction("com.android.settings.TTS_SETTINGS");
                speechEditorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(speechEditorIntent);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
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
        if (id == R.id.menu_run_migration_db)
        {
            DatabaseHelper helper = new DatabaseHelper(this);
            AppDao roomDb = AppDataBase.Companion.buildDataBase(this).appDao();
            AppDB appDB = new AppDB(helper, roomDb);
            appDB.migrateToWordsTable();
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
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.from_right_to_left_anim, R.anim.from_left_to_right_anim);
            transaction.replace(R.id.frame_to_page_fragm, translateFragm).addToBackStack(null).commit();
        }
        else if (id == R.id.nav_delete_dict)
        {
            Disposable subscribe = mainViewModel.getDictList()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> {
                        if (list != null && !list.isEmpty())
                        {
                            //noinspection Convert2Lambda
                            RemoveDictDialog.Companion.getInstance((ArrayList<String>) list, new RemoveDictDialog.IRemoveDictDialogCallback()
                            {
                                @Override
                                public void removeDictDialogButtonClickListener(@NonNull List<String> list)
                                {
                                    boolean contains = list.contains(mainViewModel.currentDict.getValue());
                                    if (contains)
                                    {
                                        mainViewModel.resetWordsList();
                                    }
                                }
                            }).show(getSupportFragmentManager(), RemoveDictDialog.TAG);
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
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, TestFragment.Companion.newInstance()).addToBackStack(null).commit();
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


