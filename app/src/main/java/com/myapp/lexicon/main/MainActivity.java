package com.myapp.lexicon.main;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
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

import com.appodeal.ads.Appodeal;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.myapp.lexicon.BuildConfig;
import com.myapp.lexicon.R;
import com.myapp.lexicon.aboutapp.AboutAppFragment;
import com.myapp.lexicon.addword.TranslateFragment;
import com.myapp.lexicon.ads.AdsExtensionsKt;
import com.myapp.lexicon.auth.AuthFragment;
import com.myapp.lexicon.auth.AuthViewModel;
import com.myapp.lexicon.auth.account.AccountFragment;
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
import com.myapp.lexicon.interfaces.FlowCallback;
import com.myapp.lexicon.main.viewmodels.UserViewModel;
import com.myapp.lexicon.models.User;
import com.myapp.lexicon.models.UserKt;
import com.myapp.lexicon.models.UserState;
import com.myapp.lexicon.models.Word;
import com.myapp.lexicon.models.currency.Currency;
import com.myapp.lexicon.schedule.AlarmScheduler;
import com.myapp.lexicon.service.PhoneUnlockedReceiver;
import com.myapp.lexicon.settings.ContainerFragment;
import com.myapp.lexicon.settings.PowerSettingsExtKt;
import com.myapp.lexicon.settings.SettingsExtKt;
import com.myapp.lexicon.viewmodels.CurrencyViewModel;
import com.myapp.lexicon.wordeditor.WordEditorActivity;
import com.myapp.lexicon.wordstests.OneOfFiveFragm;
import com.myapp.lexicon.wordstests.TestFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MainFragment.Listener, AuthFragment.AuthListener
{
    private View root;
    private NavigationView navView;
    public LinearLayout mainControlLayout;
    private Button btnViewDict;
    private TextView tvWordsCounter;
    private ImageView orderPlayView;
    private ViewPager2 mainViewPager;
    private Toolbar toolBar;

    private final String KEY_TV_WORDS_COUNTER = "tv_words_counter";

    public MainViewModel mainViewModel;
    private SpeechViewModel speechViewModel;
    private AuthViewModel authVM;
    private UserViewModel userVM;
    private Word currentWord;
    private int wordsInterval = 10;
    public BackgroundFragm backgroundFragm = null;
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

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        speechViewModel = new ViewModelProvider(this).get(SpeechViewModel.class);
        userVM = new ViewModelProvider(this).get(UserViewModel.class);
        authVM = new ViewModelProvider(this).get(AuthViewModel.class);

        authVM.getState().observe(this, result -> {
            result.onInit(() -> null);
            result.onNotRegistered(() -> {
                navView.getMenu().findItem(R.id.nav_user_reward).setTitle(R.string.text_get_reward);
                return null;
            });
            result.onSignUp(user -> {
                navView.getMenu().findItem(R.id.nav_user_reward).setTitle(R.string.text_account);
                userVM.addUserIfNotExists(user);
                return null;
            });
            result.onSignIn(user -> {
                navView.getMenu().findItem(R.id.nav_user_reward).setTitle(R.string.text_account);
                userVM.getUserFromCloud(user.getId());
                return null;
            });
            result.onSignOut(() -> {
                navView.getMenu().findItem(R.id.nav_user_reward).setTitle(R.string.text_get_reward);
                root.findViewById(R.id.tvReward).setVisibility(View.GONE);
                ExtensionsKt.showSnackBar(mainControlLayout, getString(R.string.text_you_are_signed_out), Snackbar.LENGTH_LONG);
                return null;
            });
        });

        boolean isInitialized = Appodeal.isInitialized(Appodeal.INTERSTITIAL | Appodeal.REWARDED_VIDEO);
        if (!isInitialized)
        {
            AdsExtensionsKt.adsInitialize(
                    this,
                    Appodeal.INTERSTITIAL | Appodeal.REWARDED_VIDEO,
                    () -> {
                        Appodeal.setAdRevenueCallbacks(revenueInfo -> {
                            double revenue = revenueInfo.getRevenue();
                            String currency = revenueInfo.getCurrency();
                            User user = userVM.getUser().getValue();
                            if (user != null)
                            {
                                user.setTotalRevenue(revenue);
                                user.setCurrency(currency);
                                userVM.updateUserRevenue(revenue, user);
                            }
                        });
                        return null;
                    },
                    apdInitializationErrors -> {
                        if (BuildConfig.DEBUG)
                        {
                            apdInitializationErrors.forEach(Throwable::printStackTrace);
                        }
                        return null;
                    }
            );
        }

        userVM.getState().observe(this, state -> {
            if (state instanceof UserViewModel.State.ReceivedUserData) {
                User user = ((UserViewModel.State.ReceivedUserData) state).getUser();
                buildRewardText(user);
            }
            if (state instanceof UserViewModel.State.RevenueUpdated)
            {
                User user = ((UserViewModel.State.RevenueUpdated) state).getUser();
                buildRewardText(user);
            }
        });

        userVM.collect(new FlowCallback()
        {
            @Override
            public void onResult(@Nullable UserViewModel.State result)
            {
                if (result instanceof UserViewModel.State.RevenueUpdated)
                {
                    User user = ((UserViewModel.State.RevenueUpdated) result).getUser();
                    buildRewardText(user);
                }
                if (result instanceof UserViewModel.State.PersonalDataUpdated)
                {
                    User user = ((UserViewModel.State.PersonalDataUpdated) result).getUser();
                    buildRewardText(user);
                }
            }

            @Override
            public void onCompletion(@Nullable Throwable thr)
            {}

            @Override
            public void onStart()
            {}
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

        MainFragment mainFragment = MainFragment.Companion.getInstance(this);
        getSupportFragmentManager().beginTransaction().add(R.id.frame_to_page_fragm, mainFragment).commit();

    }

    public void buildRewardText(User user) {
        SettingsExtKt.getExchangeRateFromPref(
                this,
                () -> null,
                (date, symbol, rate) -> {
                    double rewardToDisplay = UserKt.to2DigitsScale(user.getUserReward() * user.getCurrencyRate());
                    TextView tvReward = root.findViewById(R.id.tvReward);
                    String text = getString(R.string.text_your_reward).concat(" ").concat(String.valueOf(rewardToDisplay)).concat(" ").concat(symbol);
                    tvReward.setText(text);
                    tvReward.setVisibility(View.VISIBLE);
                    return null;
                },
                e -> {
                    if (BuildConfig.DEBUG) e.printStackTrace();
                    return null;
                }
        );
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
                    ExtensionsKt.showDialogAsSingleton(
                            MainActivity.this,
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
    protected void onDestroy()
    {
        Appodeal.setAdRevenueCallbacks(null);
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
        configureOptionsMenu(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.edit_word)
        {
            Word word = mainViewModel.getCurrentWord().getValue();
            Intent intent = new Intent(this, WordEditorActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(WordEditorActivity.KEY_EXTRA_DICT_NAME, btnViewDict.getText().toString());
            if (word != null)
            {
                bundle.putString(WordEditorActivity.KEY_EXTRA_EN_WORD, word.getEnglish());
                bundle.putString(WordEditorActivity.KEY_EXTRA_RU_WORD, word.getTranslate());
            }
            intent.replaceExtras(bundle);
            activityResultLauncher.launch(intent);
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
        if (id == R.id.cloud_storage) {
            showCloudDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    /** @noinspection Convert2Diamond*/
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
                        mainViewModel.refreshWordsList();
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
        if (itemId == R.id.nav_user_reward) {
            SettingsExtKt.getAuthDataFromPref(
                    this,
                    () -> {
                        AuthFragment authFragment = AuthFragment.Companion.newInstance(this);
                        transaction.replace(R.id.frame_to_page_fragm, authFragment).addToBackStack(null).commit();
                        return null;
                    },
                    (email, password) -> {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null)
                        {
                            AccountFragment accountFragment = AccountFragment.Companion.newInstance(currentUser.getUid(), password);
                            transaction.replace(R.id.frame_to_page_fragm, accountFragment).addToBackStack(null).commit();
                        } else
                        {
                            ExtensionsKt.showSnackBar(root, "Current user is null", Snackbar.LENGTH_LONG);
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
        }
        else if (itemId == R.id.nav_delete_dict)
        {
            mainViewModel.getDictList(list -> {
                if (!list.isEmpty()) {
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
        }
        else if (itemId == R.id.nav_edit)
        {
            Intent intent = new Intent(this, WordEditorActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(WordEditorActivity.KEY_EXTRA_DICT_NAME, btnViewDict.getText().toString());
            intent.replaceExtras(bundle);
            activityResultLauncher.launch(intent);
        }
        else if (itemId == R.id.nav_check_your_self)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, TestFragment.Companion.newInstance()).addToBackStack(null).commit();
        }
        else if (itemId == R.id.nav_settings)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, new ContainerFragment()).addToBackStack(null).commit();
        }
        else if (itemId == R.id.nav_evaluate_app)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getString(R.string.app_link)));
            startActivity(intent);
        }
        else if (itemId == R.id.nav_share)
        {
            new Share().doShare(this);
        }
        else if (itemId == R.id.nav_about_app)
        {
            AboutAppFragment aboutAppFragment = new AboutAppFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_to_page_fragm, aboutAppFragment).addToBackStack(null).commit();
        }
        else if (itemId == R.id.nav_exit)
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

    public void testIntervalOnChange(int value)
    {
        wordsInterval = value;
    }

    @SuppressWarnings("CodeBlock2Expr")
    private void showRemoveDictDialog(List<String> list) {
        ExtensionsKt.showDialogAsSingleton(
                MainActivity.this,
                ConfirmDialog.Companion.newInstance((dialog, binding) -> {
            binding.tvMessage.setText(getString(R.string.dialog_are_you_sure));
            binding.btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
            });
            binding.btnOk.setOnClickListener(v -> {
                mainViewModel.deleteDicts(list, integer -> {
                    if (integer > 0) {
                        ExtensionsKt.showSnackBar(mainControlLayout, getString(R.string.msg_selected_dict_removed), Snackbar.LENGTH_LONG);
                        boolean contains = list.contains(mainViewModel.currentDict.getValue());
                        if (contains)
                        {
                            mainViewModel.refreshWordsList();
                        }
                    }
                    dialog.dismiss();
                    return null;
                }, dict -> {
                    String message = getString(R.string.text_dict_not_found);
                    ExtensionsKt.showSnackBar(mainControlLayout, message, Snackbar.LENGTH_LONG);
                    return null;
                }, t -> {
                    if (t.getMessage() != null) {
                        ExtensionsKt.showSnackBar(mainControlLayout, t.getMessage(), Snackbar.LENGTH_LONG);
                    }
                    dialog.dismiss();
                    return null;
                });
            });
            return null;
        }), ConfirmDialog.Companion.getTAG());
    }

    private void showCloudDialog() {
        BottomSheetDialogFragment dialog = (BottomSheetDialogFragment)getSupportFragmentManager().findFragmentByTag(StorageDialog.Companion.getTAG());
        if (dialog == null) {
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
                                                    if (dataBase != null) {
                                                        dataBase.close();
                                                    }
                                                    File file = getDatabasePath(getString(R.string.data_base_name));
                                                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                                                    fileOutputStream.write(bytes);
                                                    fileOutputStream.close();
                                                    ExtensionsKt.showSnackBar(mainControlLayout, getString(R.string.text_dicts_restore_success), Snackbar.LENGTH_LONG);
                                                    toolBar.getMenu().findItem(R.id.cloud_storage).setVisible(false);
                                                    mainViewModel.refreshWordsList();
                                                }
                                                catch (Exception e)
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
                                            {}
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

    private void configureOptionsMenu(Menu menu) {

        boolean isCloudEnabled = SettingsExtKt.getCloudStorageEnabled(this);
        if (isCloudEnabled) {
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
                                        if (isFirstLaunch) {
                                            showCloudDialog();
                                        }
                                        menu.findItem(R.id.cloud_storage).setVisible(true);
                                        SettingsExtKt.setCheckFirstLaunch(MainActivity.this, false);
                                    }

                                    @Override
                                    public void onNotRequireSync()
                                    {
                                        SettingsExtKt.setCheckFirstLaunch(MainActivity.this, false);
                                        menu.findItem(R.id.cloud_storage).setVisible(false);
                                    }
                                }
                        );
                        return null;
                    },
                    () -> null,
                    () -> null
            );
        }
        else {
            menu.findItem(R.id.cloud_storage).setVisible(false);
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    void onAppFinish() {

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
                    }
                    else {
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
        if (storageEnabled) {

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
        if (passiveModeEnabled) {
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
        }
        else {
            finish();
        }
    }

    @Override
    public void refreshMainScreen(boolean isAdShow)
    {
        mainViewModel.refreshWordsList();
    }

    @Override
    public void onVisibleMainScreen()
    {
        wordsInterval = SettingsExtKt.getTestIntervalFromPref(this);
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    public void refreshAuthState(@NonNull User user)
    {
        CurrencyViewModel currencyVM = new ViewModelProvider(MainActivity.this).get(CurrencyViewModel.class);
        currencyVM.fetchExchangeRateFromCloud(Locale.getDefault());
        currencyVM.getCurrency().observe(this, result -> {
            result.onSuccess(obj -> {
                Currency currency = (Currency) obj;
                SettingsExtKt.saveExchangeRateToPref(this, currency);
                user.setCurrency(currency.getName());
                authVM.setState(new UserState.SignUp(user));
                return null;
            });
        });

        SettingsExtKt.checkCloudToken(
                this,
                () -> null,
                userId -> {
                    SettingsExtKt.setCloudStorageEnabled(MainActivity.this, true);
                    Menu menu = toolBar.getMenu();
                    configureOptionsMenu(menu);
                    return null;
                },
                () -> null,
                () -> null
        );

    }

    /** @noinspection CodeBlock2Expr*/
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        User user = userVM.getUser().getValue();
        if (user != null) {
            CurrencyViewModel currencyVM = new ViewModelProvider(this).get(CurrencyViewModel.class);
            currencyVM.fetchExchangeRateFromCloud(Locale.getDefault());
            currencyVM.getCurrency().observe(this, result -> {
                result.onSuccess(o -> {
                    if (o instanceof Currency currency) {
                        SettingsExtKt.saveExchangeRateToPref(this, currency);
                        userVM.updateUserRevenue(0.0, user);
                    }
                    return null;
                });
            });
        }
    }
}


