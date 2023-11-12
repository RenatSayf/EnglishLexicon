package com.myapp.lexicon.main;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.aboutapp.AboutAppFragment;
import com.myapp.lexicon.addword.TranslateFragment;
import com.myapp.lexicon.ads.AdsViewModelKt;
import com.myapp.lexicon.ads.BannerAdIds;
import com.myapp.lexicon.ads.RevenueViewModel;
import com.myapp.lexicon.auth.AuthFragment;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.auth.account.AccountFragment;
import com.myapp.lexicon.auth.account.AccountViewModel;
import com.myapp.lexicon.cloudstorage.CloudCheckWorker;
import com.myapp.lexicon.cloudstorage.DownloadDbWorker;
import com.myapp.lexicon.cloudstorage.StorageDialog;
import com.myapp.lexicon.database.AppDataBase;
import com.myapp.lexicon.databinding.DialogStorageBinding;
import com.myapp.lexicon.dialogs.ConfirmDialog;
import com.myapp.lexicon.dialogs.DictListDialog;
import com.myapp.lexicon.dialogs.OrderPlayDialog;
import com.myapp.lexicon.dialogs.RemoveDictDialog;
import com.myapp.lexicon.helpers.ExtensionsKt;
import com.myapp.lexicon.helpers.LockOrientation;
import com.myapp.lexicon.helpers.Share;
import com.myapp.lexicon.main.viewmodels.UserViewModel;
import com.myapp.lexicon.models.AppResult;
import com.myapp.lexicon.models.Revenue;
import com.myapp.lexicon.models.User;
import com.myapp.lexicon.models.UserKt;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.service.PhoneUnlockedReceiver;
import com.myapp.lexicon.settings.ContainerFragment;
import com.myapp.lexicon.settings.PowerSettingsExtKt;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.settings.SettingsViewModel;
import com.myapp.lexicon.wordeditor.WordEditorActivity;
import com.myapp.lexicon.wordstests.OneOfFiveFragm;
import com.myapp.lexicon.wordstests.TestFragment;
import com.parse.ParseUser;
import com.yandex.mobile.ads.banner.BannerAdView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

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
import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MainFragment.Listener, FragmentResultListener
{
    private View root;
    private NavigationView navView;
    public LinearLayout mainControlLayout;
    private Button btnViewDict;
    private TextView tvWordsCounter;
    private ImageView orderPlayView;
    private ViewPager2 mainViewPager;
    private final MainViewPagerAdapter pagerAdapter = new MainViewPagerAdapter();
    private Toolbar toolBar;

    private final String KEY_TV_WORDS_COUNTER = "tv_words_counter";

    public MainViewModel mainVM;
    private SpeechViewModel speechViewModel;
    public BackgroundFragm backgroundFragm = null;
    @Nullable
    private AccountFragment accountFragment;
    @Inject
    AlarmScheduler scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        root = LayoutInflater.from(this).inflate(R.layout.a_navig_main, new DrawerLayout(this));
        setContentView(root);

        toolBar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolBar);

        navView = root.findViewById(R.id.nav_view);

        PhoneUnlockedReceiver.Companion.disableBroadcast();

        NotificationManager nmg = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nmg != null)
        {
            nmg.cancelAll();
        }
        scheduler.cancel(AlarmScheduler.ONE_SHOOT_ACTION);

        getSupportFragmentManager().setFragmentResultListener(getString(R.string.KEY_NEED_REFRESH), this, this);
        getSupportFragmentManager().setFragmentResultListener(getString(R.string.KEY_TEST_INTERVAL_CHANGED), this, this);

        mainVM = new ViewModelProvider(this).get(MainViewModel.class);
        speechViewModel = new ViewModelProvider(this).get(SpeechViewModel.class);
        AuthViewModel authVM = new ViewModelProvider(this).get(AuthViewModel.class);

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
                buildRewardText(user);
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

        btnViewDict = findViewById(R.id.btnViewDict);
        btnViewDictOnClick(btnViewDict);

        orderPlayView = findViewById(R.id.order_play_icon_iv);
        btnOrderPlayOnClick(orderPlayView);

        tvWordsCounter = findViewById(R.id.tv_words_counter);

        mainViewPager = findViewById(R.id.mainViewPager);
        mainViewPager.setAdapter(pagerAdapter);
        mainViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        mainVM.wordsList.observe(this, list -> {
            if (list != null && !list.isEmpty())
            {
                int order = mainVM.getOrderPlay();
                if (order == 0 || order == 1) {
                    orderPlayView.setImageResource(R.drawable.ic_repeat_white);
                }
                else {
                    orderPlayView.setImageResource(R.drawable.ic_shuffle_white);
                }
                pagerAdapter.setItems(list);
                int displayedWordIndex = mainVM.getDisplayedWordIndex();
                if (displayedWordIndex >= 0)
                {
                    mainViewPager.setCurrentItem(displayedWordIndex, displayedWordIndex == 0);
                }
                else {
                    mainViewPager.setCurrentItem(0, true);
                }
                btnViewDict.setText(list.get(0).getDictName());
            }
        });

        mainViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            private int state = -1;
            private int position = -1;
            private Word word;
            private final MainViewPagerAdapter adapter = (MainViewPagerAdapter) mainViewPager.getAdapter();

            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                if (adapter != null)
                {
                    word = adapter.getItem(position);
                    int totalWords = adapter.getItemCount();
                    String concatText = (position + 1) + " / " + totalWords;
                    tvWordsCounter.setText(concatText);
                }
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
                if (speechViewModel.isEnSpeech().getValue() != null)
                {
                    Boolean isEnSpeech = speechViewModel.isEnSpeech().getValue();
                    if (state == 1 && isEnSpeech != null && isEnSpeech && adapter != null)
                    {
                        Word displayedWord = adapter.getItem(mainViewPager.getCurrentItem());
                        if (!displayedWord.getEnglish().equals(""))
                        {
                            speechViewModel.setSpeechProgressVisibility(View.VISIBLE);
                        }
                    }
                    Integer mControlVisibility = mainVM.getMainControlVisibility().getValue();
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
                int remainder = position % mainVM.getWordsInterval();
                int testIntervalIndex = position - remainder;
                mainVM.setIntermediateIndex(testIntervalIndex);
                List<Word> list = new ArrayList<>();
                boolean condition1 = (state == 2 && remainder == 0 && position != 0 && position > this.position);
                boolean condition2 = (state == ViewPager2.SCROLL_STATE_DRAGGING && position == mainVM.wordListSize() - 1);
                if (condition1)
                {
                    list = pagerAdapter.getItems(position - mainVM.getWordsInterval(), position - 1);
                } else if (condition2)
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
                    speechViewModel.stopSpeech();
                    mainVM.setIntermediateIndex(position);
                    mainVM.setMainControlVisibility(View.INVISIBLE);
                    speechViewModel.setSpeechProgressVisibility(View.INVISIBLE);
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
        checkBoxRuSpeak.setOnClickListener(view -> {
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
            if (!utteranceId.isEmpty())
            {
                speechViewModel.setSpeechProgressVisibility(View.INVISIBLE);
                Boolean isRu = speechViewModel.isRuSpeech().getValue();
                Word word = pagerAdapter.getItem(mainViewPager.getCurrentItem());
                if (utteranceId.equals("En") && isRu != null && isRu)
                {
                    speechViewModel.doSpeech(word.getTranslate(), Locale.getDefault());
                }
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
            if (mainVM.wordsList.getValue() != null)
            {
                Word word = mainVM.wordsList.getValue().get(position);
                String enText = word.getEnglish();
                if (isEnSpeech != null)
                {
                    speechViewModel.doSpeech(enText, Locale.US);
                    speechViewModel.setSpeechProgressVisibility(View.VISIBLE);
                }

            }
        });

        AppCompatImageButton btnReplay = findViewById(R.id.btnReplay);
        mainVM.isEndWordList.observe(this, isEnd -> {
            if (isEnd)
            {
                btnReplay.setVisibility(View.VISIBLE);
            } else btnReplay.setVisibility(View.INVISIBLE);
        });

        btnReplay.setOnClickListener(view -> mainViewPager.setCurrentItem(0, true));

        MainFragment mainFragment = MainFragment.Companion.getInstance(this);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_to_page_fragm, mainFragment).commit();

        BannerAdView bannerView = root.findViewById(R.id.bannerView);
        AdsViewModelKt.loadBanner(bannerView, BannerAdIds.BANNER_2);

        onRevenueUpdate();
        onSettingsChange();

    }

    @Override
    protected void onPause()
    {
        //mainViewModel.setDisplayedWordIndex(mainViewPager.getCurrentItem());

        super.onPause();
    }

    public void buildRewardText(User user)
    {
        Toolbar toolBar = root.findViewById(R.id.tool_bar);
        if (toolBar != null)
        {
            double rewardToDisplay = (user != null) ? UserKt.to2DigitsScale(user.getUserReward()) : 0.0;
            String text = getString(R.string.coins_bag).concat(" ")
                    .concat(getString(R.string.text_your_reward)).concat(" ")
                    .concat(String.valueOf(rewardToDisplay)).concat(" ")
                    .concat((user != null) ? user.getCurrencySymbol() : Currency.getInstance("RUB").getSymbol());
            toolBar.setSubtitle(text);

            TextView tvReward = root.findViewById(R.id.tvReward);
            if (tvReward != null)
            {
                tvReward.setText(text);
                tvReward.setVisibility(View.VISIBLE);
            }
            toolBar.setOnClickListener(view -> {
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.open();
            });
        }
    }

    private void buildRewardText2(Revenue revenue)
    {
        Toolbar toolBar = root.findViewById(R.id.tool_bar);
        if (toolBar != null)
        {
            double rewardToDisplay = (revenue != null) ? UserKt.to2DigitsScale(revenue.getReward()) : 0.0;
            String text = getString(R.string.coins_bag).concat(" ")
                    .concat(getString(R.string.text_your_reward)).concat(" ")
                    .concat(String.valueOf(rewardToDisplay)).concat(" ")
                    .concat((revenue != null) ? revenue.getCurrencySymbol() : Currency.getInstance("RUB").getSymbol());
            toolBar.setSubtitle(text);

            TextView tvReward = root.findViewById(R.id.tvReward);
            if (tvReward != null)
            {
                tvReward.setText(text);
                tvReward.setVisibility(View.VISIBLE);
            }
            toolBar.setOnClickListener(view -> {
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.open();
            });
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
        mainVM.saveCurrentWordToPref(displayedWord);
        List<Word> wordList = mainVM.wordsList.getValue();
        if (wordList != null)
        {
            int index = wordList.indexOf(displayedWord);
            if (index <= wordList.size() - 1 && index >= 0)
            {
                int i = index + 1;
                mainViewPager.setCurrentItem(i, false);
            } else mainViewPager.setCurrentItem(0, false);
            mainVM.wordsIsEnded(index + 1 == mainVM.wordListSize() - 1);
        }
        mainVM.setMainControlVisibility(View.VISIBLE);
    }

    public void testFailed(int errors)
    {
        if (errors > 0)
        {
            List<Word> wordList = mainVM.wordsList.getValue();
            if (wordList != null && mainVM.getIntermediateIndex().getValue() != null)
            {
                int newIndex = mainVM.getIntermediateIndex().getValue();
                if (newIndex <= wordList.size() - 1 && newIndex >= 0)
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
                                    int orderPlay = mainVM.getOrderPlay();
                                    Word word = new Word(1, dict, "", "", 1);
                                    mainVM.setNewPlayList(word, orderPlay);
                                    mainVM.saveCurrentWordToPref(word);
                                }
                            }),
                            DictListDialog.Companion.getTAG()
                    );
                    return null;
                }, throwable -> {
                    if (BuildConfig.DEBUG) throwable.printStackTrace();
                    return null;
                });
            }
        });
    }

    private void btnOrderPlayOnClick(ImageView view)
    {
        view.setOnClickListener(v -> {

            Word word = pagerAdapter.getItem(mainViewPager.getCurrentItem());
            int oldOrder = mainVM.getOrderPlay();
            OrderPlayDialog.Companion.getInstance(oldOrder, newOrder -> {
                if (oldOrder != newOrder)
                {
                    mainVM.setNewPlayList(word, newOrder);
                }
                else {
                    mainVM.restorePlayList(word);
                }
            }).show(getSupportFragmentManager(), OrderPlayDialog.Companion.getTAG());
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        outState.putString(KEY_TV_WORDS_COUNTER, tvWordsCounter.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop()
    {
        Word word = pagerAdapter.getItem(mainViewPager.getCurrentItem());
        mainVM.saveCurrentWordToPref(word);
        SettingsExtKt.saveOrderPlay(this, mainVM.getOrderPlay());
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.a_up_menu_main, menu);
        configureOptionsMenu(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.edit_word)
        {
            List<Word> words = mainVM.wordsList.getValue();
            if (words != null && !words.isEmpty()) {
                Word currentWord = words.get(mainViewPager.getCurrentItem());
                Intent intent = new Intent(this, WordEditorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(WordEditorActivity.KEY_EXTRA_DICT_NAME, btnViewDict.getText().toString());
                if (currentWord != null)
                {
                    bundle.putString(WordEditorActivity.KEY_EXTRA_EN_WORD, currentWord.getEnglish());
                    bundle.putString(WordEditorActivity.KEY_EXTRA_RU_WORD, currentWord.getTranslate());
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
                e.printStackTrace();
            }
        }
        if (id == R.id.menu_item_share)
        {
            new Share().doShare(this);
        }
        if (id == R.id.cloud_storage)
        {
            showCloudDialog();
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
                    if (resultCode == WordEditorActivity.requestCode)
                    {
                        mainVM.updatePlayList();
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
                            ExtensionsKt.showSnackBar(root, message, Snackbar.LENGTH_LONG);
                        }
                        return null;
                    });
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
                if (BuildConfig.DEBUG)
                {
                    throwable.printStackTrace();
                }
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
            onAppFinish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null)
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PowerSettingsExtKt.BATTERY_SETTINGS)
        {
            finish();
        }
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

    private void showCloudDialog()
    {
        BottomSheetDialogFragment dialog = (BottomSheetDialogFragment) getSupportFragmentManager().findFragmentByTag(StorageDialog.Companion.getTAG());
        if (dialog == null)
        {
            dialog = StorageDialog.Companion.newInstance(new StorageDialog.Listener()
            {
                @Override
                public void onDestroy()
                {
                    new LockOrientation(MainActivity.this).unLock();
                }

                @Override
                public void onLaunch(@NonNull DialogStorageBinding binding)
                {
                    new LockOrientation(MainActivity.this).lock();
                    binding.tvProductName.setText(getString(R.string.text_cloud_storage));
                    binding.tvPriceTitle.setText(R.string.text_dicts_have_been_found);
                    binding.btnCloudEnable.setText(R.string.text_restore);
                    binding.btnCancel.setText(getString(R.string.btn_text_cancel));
                }

                @Override
                public void onPositiveClick()
                {
                    SettingsExtKt.checkCloudToken(
                            MainActivity.this,
                            () -> null,
                            userId -> {
                                DownloadDbWorker.Companion.downloadDbFromCloud(
                                        MainActivity.this,
                                        getString(R.string.data_base_name),
                                        userId,
                                        new DownloadDbWorker.Listener()
                                        {
                                            @Override
                                            public void onSuccess(@NonNull byte[] bytes)
                                            {
                                                try
                                                {
                                                    AppDataBase dataBase = AppDataBase.Companion.getDataBase();
                                                    if (dataBase != null)
                                                    {
                                                        dataBase.close();
                                                    }
                                                    File file = getDatabasePath(getString(R.string.data_base_name));
                                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                    fileOutputStream.write(bytes);
                                                    fileOutputStream.close();
                                                    ExtensionsKt.showSnackBar(mainControlLayout, getString(R.string.text_dicts_restore_success), Snackbar.LENGTH_LONG);
                                                    toolBar.getMenu().findItem(R.id.cloud_storage).setVisible(false);
                                                    mainVM.initPlayList();
                                                } catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                    ExtensionsKt.showSnackBar(mainControlLayout, getString(R.string.text_db_restore_error), Snackbar.LENGTH_LONG);
                                                }
                                            }

                                            @Override
                                            public void onFailure(@NonNull String error)
                                            {
                                                ExtensionsKt.showSnackBar(mainControlLayout, error, Snackbar.LENGTH_LONG);
                                            }

                                            @Override
                                            public void onComplete()
                                            {
                                            }
                                        }
                                );
                                return null;
                            },
                            () -> null,
                            () -> null
                    );
                }

                @Override
                public void onCancelClick()
                {
                    toolBar.getMenu().findItem(R.id.cloud_storage).setVisible(true);
                }
            });
            dialog.show(getSupportFragmentManager(), StorageDialog.Companion.getTAG());
        }
    }

    private void configureOptionsMenu(Menu menu)
    {

        boolean isCloudEnabled = SettingsExtKt.getCloudStorageEnabled(this);
        if (isCloudEnabled)
        {
            SettingsExtKt.checkCloudToken(
                    this,
                    () -> null,
                    userId -> {
                        CloudCheckWorker.Companion.check(
                                this,
                                userId,
                                getString(R.string.data_base_name),
                                new CloudCheckWorker.Listener()
                                {
                                    @Override
                                    public void onRequireDownSync(@NonNull String token)
                                    {
                                        boolean isFirstLaunch = SettingsExtKt.getCheckFirstLaunch(MainActivity.this);
                                        if (isFirstLaunch)
                                        {
                                            showCloudDialog();
                                        }
                                        menu.findItem(R.id.cloud_storage).setVisible(true);
                                    }

                                    @Override
                                    public void onNotRequireSync()
                                    {
                                        //SettingsExtKt.setCheckFirstLaunch(MainActivity.this, false);
                                        menu.findItem(R.id.cloud_storage).setVisible(false);
                                    }
                                }
                        );
                        return null;
                    },
                    () -> null,
                    () -> null
            );
        } else
        {
            menu.findItem(R.id.cloud_storage).setVisible(false);
        }
    }

    /**
     * @noinspection deprecation
     */
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    void onAppFinish()
    {

        ExtensionsKt.alarmClockEnable(this);

        SettingsExtKt.checkUnLockedBroadcast(
                this,
                () -> {
                    PhoneUnlockedReceiver unlockedReceiver = PhoneUnlockedReceiver.Companion.getInstance();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    {
                        registerReceiver(
                                unlockedReceiver,
                                unlockedReceiver.getFilter(),
                                Context.RECEIVER_NOT_EXPORTED
                        );
                    } else
                    {
                        registerReceiver(
                                unlockedReceiver,
                                unlockedReceiver.getFilter()
                        );
                    }
                    return null;
                },
                () -> null
        );

        boolean storageEnabled = SettingsExtKt.getCloudStorageEnabled(this);
        if (storageEnabled)
        {

            SettingsExtKt.checkCloudToken(
                    this,
                    () -> null,
                    userId -> {
                        CloudCheckWorker.Companion.check(
                                this,
                                userId,
                                getString(R.string.data_base_name),
                                new CloudCheckWorker.Listener()
                                {
                                    @Override
                                    public void onBeforeChecking(@NonNull String dbName)
                                    {
                                        AppDataBase.Companion.dbClose();
                                    }
                                }
                        );
                        return null;
                    },
                    () -> null,
                    () -> null
            );
        }

        boolean passiveModeEnabled = SettingsExtKt.checkPassiveModeEnabled(this);
        if (passiveModeEnabled)
        {
            PowerSettingsExtKt.checkBatterySettings(
                    this,
                    () -> {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        intent.putExtra(PowerSettingsExtKt.KEY_BATTERY_SETTINGS, PowerSettingsExtKt.BATTERY_SETTINGS);
                        startActivityForResult(intent, PowerSettingsExtKt.BATTERY_SETTINGS);
                        return null;
                    },
                    () -> {
                        finish();
                        return null;
                    }
            );
        } else
        {
            finish();
        }
    }

    @Override
    public void onVisibleMainScreen()
    {
    }

    private void onSettingsChange()
    {
        SettingsViewModel viewModel = new ViewModelProvider(MainActivity.this).get(SettingsViewModel.class);
        viewModel.getStoragePrefHasChanged().observe(this, result -> {
            if (result)
            {
                Menu menu = toolBar.getMenu();
                configureOptionsMenu(menu);
            }
        });
    }

    /**
     * @noinspection unchecked
     */
    private void onRevenueUpdate()
    {

        RevenueViewModel revenueVM = new ViewModelProvider(MainActivity.this).get(RevenueViewModel.class);
        revenueVM.getUserRevenueLD().observe(this, result -> {
            if (result instanceof AppResult.Success<?>)
            {
                AppResult.Success<Revenue> castResult = (AppResult.Success<Revenue>) result;
                Revenue revenue = castResult.getData();
                buildRewardText2(revenue);
            }
            if (result instanceof AppResult.Error error)
            {
                ExtensionsKt.printStackTraceIfDebug((Exception) error.getError());
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
            mainVM.updatePlayList();
        }
        if (requestKey.equals(getString(R.string.KEY_TEST_INTERVAL_CHANGED))) {
            mainVM.getWordsInterval();
        }
    }
}


