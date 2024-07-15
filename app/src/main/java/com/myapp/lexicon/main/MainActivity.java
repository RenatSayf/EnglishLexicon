package com.myapp.lexicon.main;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.aboutapp.AboutAppFragment;
import com.myapp.lexicon.addword.TranslateFragment;
import com.myapp.lexicon.ads.AdsViewModel;
import com.myapp.lexicon.ads.AdsViewModelKt;
import com.myapp.lexicon.ads.BannerAdIds;
import com.myapp.lexicon.ads.RevenueViewModel;
import com.myapp.lexicon.auth.AuthFragment;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.auth.account.AccountFragment;
import com.myapp.lexicon.auth.account.AccountViewModel;
import com.myapp.lexicon.database.AppDataBase;
import com.myapp.lexicon.databinding.AContentMainBinding;
import com.myapp.lexicon.databinding.ANavigMainBinding;
import com.myapp.lexicon.databinding.LayoutMainToolbarBinding;
import com.myapp.lexicon.dialogs.ConfirmDialog;
import com.myapp.lexicon.dialogs.DictListDialog;
import com.myapp.lexicon.dialogs.OrderPlayDialog;
import com.myapp.lexicon.dialogs.RemoveDictDialog;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.Share;
import com.myapp.lexicon.main.ext.MainActivityExtKt;
import com.myapp.lexicon.main.viewmodels.UserViewModel;
import com.myapp.lexicon.models.AppResult;
import com.myapp.lexicon.models.Revenue;
import com.myapp.lexicon.models.UserKt;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.models.WordList;
import com.myapp.lexicon.repository.DataRepositoryImpl;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.service.PhoneUnlockedReceiver;
import com.myapp.lexicon.settings.ContainerFragment;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.video.constants.ConstantsKt;
import com.myapp.lexicon.video.web.YouTubeFragment;
import com.myapp.lexicon.wordeditor.WordEditorActivity;
import com.myapp.lexicon.wordstests.OneOfFiveFragm;
import com.myapp.lexicon.wordstests.TestFragment;
import com.parse.ParseUser;
import com.yandex.mobile.ads.banner.BannerAdView;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import kotlin.Pair;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MainFragment.Listener, FragmentResultListener
{
    private ANavigMainBinding binding;
    private LayoutMainToolbarBinding toolbarBinding;
    private AContentMainBinding contentBinding;
    private NavigationView navView;
    private Toolbar toolBar;
    private DrawerLayout drawerLayout;
    private TextView tvReward;
    public LinearLayout mainControlLayout;
    private Button btnViewDict;
    private TextView tvWordsCounter;
    private ImageView orderPlayView;
    private ViewPager2 mainViewPager;
    private final MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter();
    public MainViewModel mainVM;
    private SpeechViewModel speechVM;
    private RevenueViewModel revenueVM;
    public BackgroundFragm backgroundFragm = null;
    @Nullable
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ANavigMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        toolbarBinding = binding.includeLayoutMain.includeContentMain.includeToolBar;
        toolBar = toolbarBinding.layoutToolBar;
        setSupportActionBar(toolBar);

        contentBinding = binding.includeLayoutMain.includeContentMain;

        navView = binding.navView;
        navView.getMenu().findItem(R.id.nav_video_list).setVisible(ConstantsKt.getIS_VIDEO_SECTION());
        navView.setNavigationItemSelectedListener(this);
        tvReward = navView.getHeaderView(0).findViewById(R.id.tvReward);

        drawerLayout = binding.drawerLayout;
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        //noinspection deprecation
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        ImageButton btnSpeak = contentBinding.btnSpeak;

        getSupportFragmentManager().setFragmentResultListener(getString(R.string.KEY_NEED_REFRESH), this, this);
        getSupportFragmentManager().setFragmentResultListener(getString(R.string.KEY_TEST_INTERVAL_CHANGED), this, this);
        getSupportFragmentManager().setFragmentResultListener(TranslateFragment.Companion.getKEY_FRAGMENT_START(), this, this);

        mainVM = createMainViewModel();
        speechVM = createSpeechViewModel();
        speechVM = new ViewModelProvider(this).get(SpeechViewModel.class);
        AuthViewModel authVM = new ViewModelProvider(this).get(AuthViewModel.class);
        revenueVM = new ViewModelProvider(MainActivity.this).get(RevenueViewModel.class);

        authVM.getState().observe(this, result -> {
            result.onInit(() -> {
                buildRewardText(null);
                SettingsExtKt.isUserRegistered(this,
                        () -> null,
                        () -> {
                            boolean isFirstLaunch = SettingsExtKt.getCheckFirstLaunch(this);
                            if (isFirstLaunch)
                            {
                                ExtensionsKt.showSignUpBenefitsDialog(
                                        this,
                                        () -> {
                                            SettingsExtKt.setCheckFirstLaunch(MainActivity.this, false);
                                            AuthFragment authFragment = AuthFragment.Companion.newInstance();
                                            getSupportFragmentManager()
                                                    .beginTransaction()
                                                    .replace(R.id.frame_to_page_fragm, authFragment)
                                                    .addToBackStack(null)
                                                    .commit();
                                            return null;
                                        },
                                        () -> {
                                            SettingsExtKt.setCheckFirstLaunch(MainActivity.this, false);
                                            return null;
                                        }
                                );
                            }
                            return null;
                        }
                );
                return null;
            });
            result.onNotRegistered(() -> {
                navView.getMenu().findItem(R.id.nav_user_reward).setTitle(R.string.text_get_reward);
                buildRewardText(null);
                return null;
            });
            result.onSignUp(user -> {
                navView.getMenu().findItem(R.id.nav_user_reward).setTitle(R.string.text_account);
                return null;
            });
            result.onSignIn(user -> {
                navView.getMenu().findItem(R.id.nav_user_reward).setTitle(R.string.text_account);
                buildRewardText(new Revenue(user.getUserReward(), user.getReservedPayment(), user.getCurrency(), user.getCurrencySymbol()));
                SettingsExtKt.setAdsIsEnabled(this, user.isAdsEnabled());
                if (!user.isAdsEnabled() && !user.getMessage().isEmpty()) {
                    MainActivityExtKt.showWarningDialog(this, user.getMessage());
                }
                return null;
            });
            result.onSignOut(() -> {
                handleSignOutAction();
                ExtensionsKt.showSnackBar(mainControlLayout, getString(R.string.text_you_are_signed_out), Snackbar.LENGTH_LONG);
                return null;
            });
            result.onAccountDeleted(() -> {
                handleSignOutAction();
                ExtensionsKt.showSnackBar(mainControlLayout, getString(R.string.text_account_has_been_deleted), Snackbar.LENGTH_LONG);
                return null;
            });
        });

        btnViewDict = contentBinding.btnViewDict;
        btnViewDictOnClick(btnViewDict);

        orderPlayView = contentBinding.orderPlayIconIv;
        btnOrderPlayOnClick(orderPlayView);

        tvWordsCounter = contentBinding.tvWordsCounter;

        mainViewPager = contentBinding.mainViewPager;
        mainViewPager.setAdapter(pagerAdapter);
        mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        mainVM.wordsList.observe(this, list -> {
            if (list != null && !list.getWords().isEmpty())
            {
                int order = ExtensionsKt.checkSorting(list.getWords());
                SettingsExtKt.saveOrderPlay(this, order);
                if (order == 0) {
                    orderPlayView.setImageResource(R.drawable.ic_repeat_white);
                    orderPlayView.setScaleX(1f);
                }
                else if (order == 1) {
                    orderPlayView.setImageResource(R.drawable.ic_repeat_white);
                    orderPlayView.setScaleX(-1f);
                }
                else {
                    orderPlayView.setImageResource(R.drawable.ic_shuffle_white);
                }
                pagerAdapter.setItems(list.getWords());
                int pagerPosition = list.getBookmark();
                if (pagerPosition >= 0)
                {
                    mainViewPager.setCurrentItem(pagerPosition, pagerPosition == 0);
                }
                else {
                    mainViewPager.setCurrentItem(0, true);
                }
                Word word = pagerAdapter.getItem(pagerPosition);
                SettingsExtKt.saveWordToPref(MainActivity.this, word, pagerPosition);
                buildCountersText(pagerPosition);
                btnViewDict.setText(list.getWords().get(0).getDictName());
            }
        });

        mainViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            private int state = -1;
            private int position = -1;
            private Word word;

            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                word = pagerAdapter.getItem(position);
                buildCountersText(position);
                if (position < mainVM.wordListSize() - 1)
                {
                    mainVM.wordsIsEnded(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
                super.onPageScrollStateChanged(state);
                this.state = state;
                if (speechVM.isEnSpeech().getValue() != null)
                {
                    Boolean isEnSpeech = speechVM.isEnSpeech().getValue();
                    if (state == 1 && isEnSpeech != null && isEnSpeech)
                    {
                        Word displayedWord = pagerAdapter.getItem(mainViewPager.getCurrentItem());
                        if (displayedWord != null && !displayedWord.getEnglish().isEmpty())
                        {
                            speechVM.setSpeechProgressVisibility(View.VISIBLE);
                        }
                    }
                    Integer mControlVisibility = mainVM.getMainControlVisibility().getValue();
                    if (isEnSpeech != null && isEnSpeech && word != null && state == 0 && mControlVisibility != null && mControlVisibility == View.VISIBLE)
                    {
                        speechVM.doSpeech(word.getEnglish(), Locale.US);
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                int remainder = position % mainVM.getWordsInterval();
                int testIntervalIndex = position - remainder;
                mainVM.setIntermediateIndex(testIntervalIndex);
                List<Word> list = new ArrayList<>();
                boolean condition1 = (state == 2 && remainder == 0 && position != 0 && position > this.position);
                boolean condition2 = (state == ViewPager2.SCROLL_STATE_DRAGGING && position == mainVM.wordListSize() - 1);
                if (condition1 && pagerAdapter.getItemCount() > 1)
                {
                    list = pagerAdapter.getItems(position - mainVM.getWordsInterval(), position - 1);
                } else if (condition2 && pagerAdapter.getItemCount() > 1)
                {
                    int lastIndex = pagerAdapter.getItemCount() - 1;
                    int firstIndex = -1;
                    if (mainVM.getIntermediateIndex().getValue() != null)
                    {
                        firstIndex = mainVM.getIntermediateIndex().getValue();
                    }
                    if (firstIndex < 0) firstIndex = 0;
                    list = pagerAdapter.getItems(firstIndex, lastIndex);
                }
                if ((condition1 || condition2) && list.size() > 1)
                {
                    speechVM.stopSpeech();
                    mainVM.setIntermediateIndex(position);
                    mainVM.setMainControlVisibility(View.INVISIBLE);
                    speechVM.setSpeechProgressVisibility(View.INVISIBLE);
                    Toast.makeText(MainActivity.this, getString(R.string.text_test_knowledge), Toast.LENGTH_LONG).show();
                    OneOfFiveFragm testFragment = OneOfFiveFragm.newInstance(list);
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.from_right_to_left_anim, R.anim.from_left_to_right_anim)
                            .addToBackStack(null)
                            .add(R.id.frame_to_page_fragm, testFragment)
                            .commit();
                    mainViewPager.setCurrentItem(position - 1);
                    return;
                }
                this.position = position;
            }
        });

        mainVM.getMainControlVisibility().observe(this, visibility -> {
            mainControlLayout = contentBinding.mainControlLayout;
            mainControlLayout.setVisibility(visibility);
        });

        if (savedInstanceState == null)
        {
            backgroundFragm = new BackgroundFragm();
            getSupportFragmentManager().beginTransaction().replace(R.id.background_fragment, backgroundFragm).commit();
        }

        CheckBox checkBoxEnView = contentBinding.checkBoxEnSpeak;
        //noinspection CodeBlock2Expr
        checkBoxEnView.setOnCheckedChangeListener((compoundButton, b) -> {
            speechVM.enableEnSpeech(b);
        });

        speechVM.isEnSpeech().observe(this, checked -> {
            checkBoxEnView.setChecked(checked);
            if (!checked)
            {
                speechVM.setSpeechProgressVisibility(View.INVISIBLE);
            }
        });

        speechVM.getEnCheckboxEnable().observe(this, enabled -> {
            checkBoxEnView.setEnabled(enabled);
            btnSpeak.setEnabled(enabled);
        });

        CheckBox checkBoxRuSpeak = contentBinding.checkBoxRuSpeak;
        checkBoxRuSpeak.setOnClickListener(view -> {
            CheckBox checkBox = (CheckBox) view;
            speechVM.enableRuSpeech(checkBox.isChecked());
            if (checkBox.isChecked())
            {
                Toast.makeText(MainActivity.this, R.string.text_ru_speech_on, Toast.LENGTH_SHORT).show();
            } else
            {
                Toast.makeText(MainActivity.this, R.string.text_ru_speech_off, Toast.LENGTH_SHORT).show();
            }
        });

        speechVM.isRuSpeech().observe(this, checked -> {
            //noinspection Convert2MethodRef
            checkBoxRuSpeak.setChecked(checked);
        });

        speechVM.getRuCheckboxEnable().observe(this, checkBoxRuSpeak::setEnabled);

        ProgressBar speechProgress = contentBinding.speechProgress;
        //noinspection CodeBlock2Expr
        speechVM.getSpeechStartId().observe(MainActivity.this, utteranceId ->
        {
            speechVM.setSpeechProgressVisibility(View.INVISIBLE);
        });

        speechVM.getSpeechDoneId().observe(MainActivity.this, utteranceId ->
        {
            if (!utteranceId.isEmpty())
            {
                speechVM.setSpeechProgressVisibility(View.INVISIBLE);
                Boolean isRu = speechVM.isRuSpeech().getValue();
                Word word = pagerAdapter.getItem(mainViewPager.getCurrentItem());
                if (word != null && utteranceId.equals("En") && isRu != null && isRu)
                {
                    speechVM.doSpeech(word.getTranslate(), Locale.getDefault());
                }
            }
        });

        //noinspection CodeBlock2Expr
        speechVM.getSpeechError().observe(this, err -> {
            speechVM.setSpeechProgressVisibility(View.INVISIBLE);
        });

        speechVM.getSpeechProgressVisibility().observe(this, v -> {
            speechProgress.setVisibility(v);
            if (v == View.VISIBLE)
                btnSpeak.setVisibility(View.INVISIBLE);
            else btnSpeak.setVisibility(View.VISIBLE);
        });


        btnSpeak.setOnClickListener(view -> {
            int position = mainViewPager.getCurrentItem();
            Boolean isEnSpeech = speechVM.isEnSpeech().getValue();
            if (mainVM.wordsList.getValue() != null)
            {
                Word word = mainVM.wordsList.getValue().getWords().get(position);
                String enText = word.getEnglish();
                if (isEnSpeech != null)
                {
                    speechVM.doSpeech(enText, Locale.US);
                    speechVM.setSpeechProgressVisibility(View.VISIBLE);
                }

            }
        });

        AppCompatImageButton btnReplay = contentBinding.btnReplay;
        mainVM.isEndWordList.observe(this, isEnd -> {
            if (isEnd)
            {
                btnReplay.setVisibility(View.VISIBLE);
            } else btnReplay.setVisibility(View.INVISIBLE);
        });

        btnReplay.setOnClickListener(view -> mainViewPager.setCurrentItem(0, true));

        MainFragment mainFragment = MainFragment.Companion.getInstance(this);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_to_page_fragm, mainFragment).commit();

        BannerAdView bannerView = contentBinding.bannerView;
        AdsViewModelKt.loadBanner(bannerView, BannerAdIds.BANNER_2);

        onRevenueUpdate();

    }

    private SpeechViewModel createSpeechViewModel()
    {
        SpeechViewModel.Factory factory = new SpeechViewModel.Factory(this.getApplication());
        return new ViewModelProvider(this, factory).get(SpeechViewModel.class);
    }

    @NonNull
    private MainViewModel createMainViewModel() {
        MainViewModel.Factory factory = new MainViewModel.Factory(this.getApplication());
        return new ViewModelProvider(this, factory).get(MainViewModel.class);
    }

    private void buildRewardText(Revenue revenue)
    {
        if (toolBar != null)
        {
            double rewardToDisplay = (revenue != null) ? UserKt.to2DigitsScale(revenue.getReward()) : 0.0;
            String text = getString(R.string.coins_bag).concat(" ")
                    .concat(getString(R.string.text_your_reward)).concat(" ")
                    .concat(String.valueOf(rewardToDisplay)).concat(" ")
                    .concat((revenue != null) ? revenue.getCurrencySymbol() : Currency.getInstance("RUB").getSymbol());
            TextView tvSubTitle = toolbarBinding.tvSubtitle;
            tvSubTitle.setText(text);

            if (tvReward != null)
            {
                tvReward.setText(text);
                tvReward.setVisibility(View.VISIBLE);
            }
            toolBar.setOnClickListener(view -> {
                if (drawerLayout != null)
                {
                    drawerLayout.open();
                }
            });
        }
    }

    private void buildCountersText(int position) {
        if (pagerAdapter != null)
        {
            int totalWords = pagerAdapter.getItemCount();
            String concatText = (position + 1) + " / " + totalWords;
            tvWordsCounter.setText(concatText);
        }
    }

    private void handleSignOutAction()
    {
        navView.getMenu().findItem(R.id.nav_user_reward).setTitle(R.string.text_get_reward);
        buildRewardText(null);
    }

    public void testPassed()
    {
        Word displayedWord = pagerAdapter.getItem(mainViewPager.getCurrentItem());
        SettingsExtKt.saveWordToPref(this, displayedWord, mainViewPager.getCurrentItem());
        WordList wordList = mainVM.wordsList.getValue();
        if (wordList != null)
        {
            int index = wordList.getWords().indexOf(displayedWord);
            if (index <= wordList.getWords().size() - 1 && index >= 0)
            {
                int i = index + 1;
                mainViewPager.setCurrentItem(i, false);
            } else mainViewPager.setCurrentItem(0, false);
            mainVM.wordsIsEnded(index + 1 == mainVM.wordListSize() - 1);
            mainVM.setMainControlVisibility(View.VISIBLE);
        }
    }

    public void testFailed(int errors)
    {
        if (errors > 0)
        {
            WordList wordList = mainVM.wordsList.getValue();
            if (wordList != null && mainVM.getIntermediateIndex().getValue() != null)
            {
                int newIndex = mainVM.getIntermediateIndex().getValue();
                if (newIndex <= wordList.getWords().size() - 1 && newIndex >= 0)
                {
                    mainViewPager.setCurrentItem(newIndex, true);
                } else mainViewPager.setCurrentItem(0, true);
            }
        }
        mainVM.setMainControlVisibility(View.VISIBLE);
    }

    @SuppressWarnings("Convert2Lambda")
    private void btnViewDictOnClick(Button button)
    {
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mainVM.getDictList(list -> {

                    String buttonText = button.getText().toString();
                    int index = list.indexOf(buttonText);
                    if (index >= 0)
                    {
                        String item = list.remove(index);
                        list.add(0, item);
                    }
                    ExtensionsKt.showDialogAsSingleton(
                            MainActivity.this,
                            DictListDialog.Companion.getInstance(list, new DictListDialog.ISelectItemListener()
                            {
                                @Override
                                public void dictListItemOnSelected(@NonNull String dict)
                                {
                                    int orderPlay = SettingsExtKt.getOrderPlayFromPref(MainActivity.this);
                                    mainVM.setNewPlayList(dict, orderPlay);
                                }
                            }),
                            DictListDialog.Companion.getTAG()
                    );
                    return null;
                }, throwable -> {
                    ExtensionsKt.printStackTraceIfDebug(throwable);
                    return null;
                });
            }
        });
    }

    private void btnOrderPlayOnClick(ImageView view)
    {
        view.setOnClickListener(v -> {

            Word word = pagerAdapter.getItem(mainViewPager.getCurrentItem());
            if (word != null)
            {
                int oldOrder = SettingsExtKt.getOrderPlayFromPref(MainActivity.this);
                OrderPlayDialog.Companion.getInstance(oldOrder, newOrder -> {
                    if (oldOrder != newOrder)
                    {
                        mainVM.setNewPlayList(word.getDictName(), newOrder);
                    }
                    else {
                        mainVM.restorePlayList(word);
                    }
                }).show(getSupportFragmentManager(), OrderPlayDialog.Companion.getTAG());
            }
        });
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart()
    {
        super.onStart();

        AppDataBase dataBase = AppDataBase.Companion.getDataBase();
        if (dataBase == null) {
            AppDataBase dbInstance = AppDataBase.Companion.getDbInstance(this);
            DataRepositoryImpl repository = new DataRepositoryImpl(dbInstance.appDao());
            mainVM.injectDependencies(repository);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            PhoneUnlockedReceiver.Companion.disableBroadcast();

            NotificationManager nmg = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nmg != null)
            {
                nmg.cancelAll();
            }
            AlarmScheduler scheduler = new AlarmScheduler(this);
            scheduler.cancel(AlarmScheduler.ONE_SHOOT_ACTION);
        }
    }

    @Override
    protected void onStop()
    {
        Word word = pagerAdapter.getItem(mainViewPager.getCurrentItem());
        SettingsExtKt.saveWordToPref(this, word, mainViewPager.getCurrentItem());

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.a_up_menu_main, menu);
        if (BuildConfig.DEBUG) {
            menu.add(getString(R.string.test_crash));
            menu.add(R.string.test_button);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (BuildConfig.DEBUG)
        {
            CharSequence title = item.getTitle();
            if (title != null && title.equals(getString(R.string.test_crash))) {
                throw new RuntimeException("Test Crash");
            }
            if (title != null && title.equals(getString(R.string.test_button))) {
                return false;
            }
        }
        int id = item.getItemId();
        if (id == R.id.edit_word)
        {
            WordList wordList = mainVM.wordsList.getValue();
            if (wordList != null && !wordList.getWords().isEmpty()) {
                Word currentWord = wordList.getWords().get(mainViewPager.getCurrentItem());
                Intent intent = new Intent(this, WordEditorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(WordEditorActivity.KEY_EXTRA_DICT_NAME, btnViewDict.getText().toString());
                if (currentWord != null)
                {
                    bundle.putString(WordEditorActivity.KEY_EXTRA_WORD, currentWord.toString());
                }
                intent.replaceExtras(bundle);
                activityResultLauncher.launch(intent);
            }
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
                ExtensionsKt.printStackTraceIfDebug(e);
            }
        }
        if (id == R.id.menu_item_share)
        {
            new Share().doShare(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @noinspection Convert2Diamond
     */
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    int resultCode = result.getResultCode();
                    if (resultCode == WordEditorActivity.NEED_UPDATE_PLAY_LIST)
                    {
                        SettingsExtKt.getWordFromPref(
                                MainActivity.this,
                                () -> null,
                                (word, mark) -> {
                                    int orderPlay = SettingsExtKt.getOrderPlayFromPref(MainActivity.this);
                                    mainVM.updatePlayList(word, mark, orderPlay);
                                    return null;
                                },
                                e -> null
                        );
                    }
                }
            });


    @SuppressWarnings("Convert2Lambda")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.from_right_to_left_anim, R.anim.from_left_to_right_anim);
        int itemId = item.getItemId();
        if (itemId == R.id.nav_user_reward)
        {
            SettingsExtKt.getAuthDataFromPref(
                    this,
                    () -> {
                        AuthFragment authFragment = AuthFragment.Companion.newInstance();
                        transaction.replace(R.id.frame_to_page_fragm, authFragment).addToBackStack(null).commit();
                        return null;
                    },
                    (email, password) -> {
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        if (currentUser != null)
                        {
                            accountFragment = AccountFragment.Companion.newInstance(
                                    AuthViewModel.class,
                                    AccountViewModel.class,
                                    UserViewModel.class
                            );
                            transaction.replace(R.id.frame_to_page_fragm, accountFragment)
                                    .addToBackStack(null)
                                    .commit();
                        } else
                        {
                            AuthFragment authFragment = AuthFragment.Companion.newInstance();
                            transaction.replace(R.id.frame_to_page_fragm, authFragment).addToBackStack(null).commit();
                        }
                        return null;
                    },
                    error -> {
                        String message = error.getMessage();
                        if (message != null)
                        {
                            ExtensionsKt.showSnackBar(binding.getRoot(), message, Snackbar.LENGTH_LONG);
                        }
                        return null;
                    });
        }
        if (itemId == R.id.nav_video_list)
        {
            YouTubeFragment videoListFragment = YouTubeFragment.Companion.newInstance();
            transaction.replace(R.id.frame_to_page_fragm, videoListFragment).addToBackStack(null).commit();
        }
        if (itemId == R.id.nav_add_word)
        {
            TranslateFragment translateFragm = TranslateFragment.Companion.getInstance("");
            transaction.replace(R.id.frame_to_page_fragm, translateFragm).addToBackStack(null).commitAllowingStateLoss();
        } else if (itemId == R.id.nav_delete_dict)
        {
            mainVM.getDictList(list -> {
                if (!list.isEmpty())
                {
                    ExtensionsKt.showDialogAsSingleton(
                            this,
                            RemoveDictDialog.Companion.getInstance((ArrayList<String>) list, new RemoveDictDialog.Listener()
                            {
                                @Override
                                public void onRemoveButtonClick(@NonNull List<String> list)
                                {
                                    showRemoveDictDialog(list);
                                }
                            }),
                            RemoveDictDialog.TAG);
                }
                return null;
            }, throwable -> {
                ExtensionsKt.printStackTraceIfDebug(throwable);
                return null;
            });
        } else if (itemId == R.id.nav_edit)
        {
            Intent intent = new Intent(this, WordEditorActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(WordEditorActivity.KEY_EXTRA_DICT_NAME, btnViewDict.getText().toString());
            intent.replaceExtras(bundle);
            activityResultLauncher.launch(intent);
        } else if (itemId == R.id.nav_check_your_self)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_to_page_fragm, TestFragment.Companion.newInstance())
                    .addToBackStack(null)
                    .commit();
        } else if (itemId == R.id.nav_settings)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, new ContainerFragment()).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_evaluate_app)
        {
            SettingsExtKt.goToAppStore(MainActivity.this);
        } else if (itemId == R.id.nav_share)
        {
            new Share().doShare(this);
        } else if (itemId == R.id.nav_about_app)
        {
            AboutAppFragment aboutAppFragment = new AboutAppFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, aboutAppFragment).addToBackStack(null).commit();
        } else if (itemId == R.id.nav_exit)
        {
            ExtensionsKt.setServiceBroadcasts(MainActivity.this);
            finish();
        }

        if (drawerLayout != null)
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @SuppressWarnings("CodeBlock2Expr")
    private void showRemoveDictDialog(List<String> list)
    {
        LockOrientation locker = new LockOrientation(this);
        ExtensionsKt.showDialogAsSingleton(
                MainActivity.this,
                ConfirmDialog.Companion.newInstance((dialog, binding) -> {
                    locker.lock();
                    binding.tvMessage.setText(getString(R.string.dialog_are_you_sure));
                    binding.btnCancel.setOnClickListener(v -> {
                        dialog.dismiss();
                    });
                    binding.btnOk.setOnClickListener(v -> {
                        mainVM.deleteDicts(list, integer -> {
                            if (integer > 0)
                            {
                                ExtensionsKt.showSnackBar(mainControlLayout, getString(R.string.msg_selected_dict_removed), Snackbar.LENGTH_LONG);
                                boolean contains = list.contains(btnViewDict.getText().toString());
                                if (contains)
                                {
                                    SettingsExtKt.saveWordToPref(MainActivity.this, null, -1);
                                    mainVM.initPlayList();
                                }
                            }
                            dialog.dismiss();
                            return null;
                        }, dict -> {
                            String message = getString(R.string.text_dict_not_found);
                            ExtensionsKt.showSnackBar(mainControlLayout, message, Snackbar.LENGTH_LONG);
                            return null;
                        }, t -> {
                            if (t != null && t.getMessage() != null)
                            {
                                ExtensionsKt.showSnackBar(mainControlLayout, t.getMessage(), Snackbar.LENGTH_LONG);
                            }
                            locker.unLock();
                            dialog.dismiss();
                            return null;
                        });
                    });
                    return null;
                }), ConfirmDialog.Companion.getTAG());
    }

    @Override
    public void onVisibleMainScreen()
    {
    }

    /**
     * @noinspection unchecked
     */
    private void onRevenueUpdate()
    {
        revenueVM.getUserRevenueLD().observe(this, result -> {
            if (result instanceof AppResult.Success<?>)
            {
                AppResult.Success<Revenue> castResult = (AppResult.Success<Revenue>) result;
                Revenue revenue = castResult.getData();
                buildRewardText(revenue);
            }
            if (result instanceof AppResult.Error error)
            {
                ExtensionsKt.printStackTraceIfDebug((Exception) error.getError());
            }
        });

        AdsViewModel adsVM = new ViewModelProvider(this).get(AdsViewModel.class);
        adsVM.getInterstitialAdState().observe(this, adState -> {
            if (adState instanceof AdsViewModel.AdState.Dismissed) {
                double bonus = ((AdsViewModel.AdState.Dismissed) adState).getBonus();
                if (bonus > 0.0)
                {
                    TextView tvSubTitle = toolbarBinding.tvSubtitle;
                    int x = tvSubTitle.getRight();
                    int top = tvSubTitle.getTop();
                    int bottom = tvSubTitle.getBottom();
                    Pair<Integer, Integer> coordinates = new Pair<>(x, top + ((top - bottom)/2));
                    FrameLayout frameLayout = contentBinding.frameToPageFragm;
                    com.myapp.lexicon.ads.ext.ExtensionsKt.showUserRewardAnimatedly(
                            frameLayout,
                            String.valueOf(bonus),
                            coordinates
                    );
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result)
    {
        if (requestKey.equals(getString(R.string.KEY_NEED_REFRESH))) {
            SettingsExtKt.getWordFromPref(
                    MainActivity.this,
                    () -> null,
                    (word, mark) -> {
                        int orderPlay = SettingsExtKt.getOrderPlayFromPref(MainActivity.this);
                        mainVM.updatePlayList(word, mark, orderPlay);
                        return null;
                    },
                    e -> null
            );
        }

        if (requestKey.equals(getString(R.string.KEY_TEST_INTERVAL_CHANGED))) {
            mainVM.getWordsInterval();
        }

        if (requestKey.equals(TranslateFragment.Companion.getKEY_FRAGMENT_START())) {
            int currentIndex = mainViewPager.getCurrentItem();
            Word word = pagerAdapter.getItem(currentIndex);
            SettingsExtKt.saveWordToPref(this, word, mainViewPager.getCurrentItem());
        }
    }
}


