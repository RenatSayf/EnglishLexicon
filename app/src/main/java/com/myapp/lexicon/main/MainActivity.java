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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.aboutapp.AboutAppFragment;
import com.myapp.lexicon.addword.TranslateFragment;
import com.myapp.lexicon.ads.AdsExtensionsKt;
import com.myapp.lexicon.cloudstorage.UploadDbWorker;
import com.myapp.lexicon.database.AppDB;
import com.myapp.lexicon.database.AppDao;
import com.myapp.lexicon.database.AppDataBase;
import com.myapp.lexicon.database.DatabaseHelper;
import com.myapp.lexicon.dialogs.DictListDialog;
import com.myapp.lexicon.dialogs.OrderPlayDialog;
import com.myapp.lexicon.dialogs.RemoveDictDialog;
import com.myapp.lexicon.helpers.AppBus;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.JavaKotlinMediator;
import com.myapp.lexicon.helpers.Share;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.service.LexiconService;
import com.myapp.lexicon.settings.ContainerFragment;
import com.myapp.lexicon.wordeditor.WordEditorActivity;
import com.myapp.lexicon.wordstests.OneOfFiveFragm;
import com.myapp.lexicon.wordstests.TestFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
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
    private Word currentWord;
    private int wordsInterval = Integer.MAX_VALUE;
    public BackgroundFragm backgroundFragm = null;

    @Inject
    AlarmScheduler scheduler;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        View root = LayoutInflater.from(this).inflate(R.layout.a_navig_main, new DrawerLayout(this));
        setContentView(root);

        Toolbar toolBar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

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
            backgroundFragm = new BackgroundFragm();
            getSupportFragmentManager().beginTransaction().replace(R.id.background_fragment, backgroundFragm).commit();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

    @SuppressWarnings("Convert2Lambda")
    private void btnViewDictOnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mainViewModel.getDictList(list -> {

                    String buttonText = button.getText().toString();
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
                    return null;
                }, throwable -> {
                    if (BuildConfig.DEBUG) throwable.printStackTrace();
                    return null;
                });
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
    }

    @Override
    protected void onDestroy()
    {
        try
        {
            mainViewModel.saveCurrentWordToPref(currentWord);
        } catch (Exception e)
        {
            if (BuildConfig.DEBUG) e.printStackTrace();
        }
        super.onDestroy();
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

    @SuppressWarnings("Convert2Lambda")
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
            mainViewModel.getDictList(list -> {
                if (!list.isEmpty()) {
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
                return null;
            }, throwable -> {
                if (BuildConfig.DEBUG)
                {
                    throwable.printStackTrace();
                }
                return null;
            });
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
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, new ContainerFragment()).addToBackStack(null).commit();
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
            ExtensionsKt.alarmClockEnable(this);
            AdsExtensionsKt.getAdvertisingID(this, adsId -> {
                        UploadDbWorker.Companion.uploadDbToCloud(
                                this,
                                this.getString(R.string.data_base_name),
                                adsId,
                                null);
                        return null;
                    }, () -> null,
                    error -> null,
                    () -> null);

            if (backgroundFragm != null && backgroundFragm.yandexAd != null)
            {
                new JavaKotlinMediator().showInterstitialAd(backgroundFragm.yandexAd, this::finish);
            } else
            {
                finish();
            }
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


