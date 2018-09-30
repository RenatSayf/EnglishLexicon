package com.myapp.lexicon.wordstests;


import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.myapp.lexicon.R;
import com.myapp.lexicon.settings.AppData;
import com.myapp.lexicon.settings.AppSettings;

public class Tests extends AppCompatActivity
{
    private ImageButton buttonFindPair, buttonListenEndClick, buttonOneOfFive;
    private FindPairFragment findPairFragment;
    private ListenEndClickFragment listenEndClickFragment;
    private OneOfFiveTest oneOfFiveTest;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.t_layout_tests);
            Toolbar toolbar = findViewById(R.id.toolbar_word_editor);
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null)
            {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            buttonFindPair = findViewById(R.id.btn_find_pair);
            buttonListenEndClick = findViewById(R.id.btn_select_word_test);
            buttonOneOfFive = findViewById(R.id.btn_test_1of5);
            button_OnClick();

            if (AppData.getInstance().isAdMob())
            {
                if (AppData.getInstance().isOnline(Tests.this))
                {
                    if (savedInstanceState == null)
                    {
                        BannerFragmentTests bannerFragment = new BannerFragmentTests();
                        getSupportFragmentManager().beginTransaction().replace(R.id.banner_frame_tests, bannerFragment).commit();
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            finish();
        }
    }

    private void button_OnClick()
    {
        buttonFindPair.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                findPairFragment = (FindPairFragment) getSupportFragmentManager().findFragmentByTag(FindPairFragment.TAG);
                if (findPairFragment == null)
                {
                    findPairFragment = new FindPairFragment();
                }

                final AppSettings appSettings = new AppSettings(Tests.this);
                final Bundle bundle = appSettings.getTestFragmentState(FindPairFragment.TAG);

                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.find_pair_fragment, findPairFragment);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                transaction.addToBackStack(null);

                if (bundle.getInt(appSettings.KEY_COUNTER_RIGHT_ANSWER) > 0)
                {
                    DialogWarning dialogWarning = new DialogWarning();
                    Bundle dialogBundle = new Bundle();
                    dialogBundle.putString(dialogWarning.KEY_MESSAGE, getString(R.string.you_have_uncompleted_test));
                    dialogBundle.putString(dialogWarning.KEY_TEXT_OK_BUTTON, getString(R.string.text_continue));
                    dialogBundle.putString(dialogWarning.KEY_TEXT_NO_BUTTON, getString(R.string.text_from_the_beginning));
                    dialogBundle.putBoolean(dialogWarning.KEY_IS_NEUTRAL_BTN, true);
                    dialogWarning.setArguments(dialogBundle);
                    dialogWarning.setCancelable(false);
                    dialogWarning.setListener(new DialogWarning.IDialogResult()
                    {
                        @Override
                        public void dialogListener(boolean result)
                        {
                            if (result)
                            {
                                 findPairFragment.setArguments(appSettings.getTestFragmentState(FindPairFragment.TAG));
                            }
                            else
                            {
                                appSettings.saveTestFragmentState(FindPairFragment.TAG, null);
                                findPairFragment.setArguments(null);
                            }
                            transaction.commit();
                        }
                    });
                    dialogWarning.show(getSupportFragmentManager(), dialogWarning.TAG);
                }
                else
                {
                    transaction.commit();
                }

            }
        });


        buttonOneOfFive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                oneOfFiveTest = (OneOfFiveTest) getSupportFragmentManager().findFragmentByTag(OneOfFiveTest.TAG);
                if (oneOfFiveTest == null)
                {
                    oneOfFiveTest = new OneOfFiveTest();
                }

                final AppSettings appSettings = new AppSettings(Tests.this);
                final Bundle bundle = appSettings.getTestFragmentState(OneOfFiveTest.TAG);

                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_1of5, oneOfFiveTest);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                transaction.addToBackStack(null);

                if (bundle.getInt(appSettings.KEY_COUNTER_RIGHT_ANSWER) > 0)
                {
                    DialogWarning dialogWarning = new DialogWarning();
                    Bundle dialogBundle = new Bundle();
                    dialogBundle.putString(dialogWarning.KEY_MESSAGE, getString(R.string.you_have_uncompleted_test));
                    dialogBundle.putString(dialogWarning.KEY_TEXT_OK_BUTTON, getString(R.string.text_continue));
                    dialogBundle.putString(dialogWarning.KEY_TEXT_NO_BUTTON, getString(R.string.text_from_the_beginning));
                    dialogBundle.putBoolean(dialogWarning.KEY_IS_NEUTRAL_BTN, true);
                    dialogWarning.setArguments(dialogBundle);
                    dialogWarning.setCancelable(false);
                    dialogWarning.setListener(new DialogWarning.IDialogResult()
                    {
                        @Override
                        public void dialogListener(boolean result)
                        {
                            if (result)
                            {
                                oneOfFiveTest.setArguments(appSettings.getTestFragmentState(OneOfFiveTest.TAG));
                            }
                            else
                            {
                                appSettings.saveTestFragmentState(OneOfFiveTest.TAG, null);
                                oneOfFiveTest.setArguments(null);
                            }
                            transaction.commit();
                        }
                    });
                    dialogWarning.show(getSupportFragmentManager(), dialogWarning.TAG);
                }
                else
                {
                    transaction.commit();
                }
            }
        });


        buttonListenEndClick.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listenEndClickFragment = (ListenEndClickFragment) getSupportFragmentManager().findFragmentByTag(ListenEndClickFragment.TAG);
                if (listenEndClickFragment == null)
                {
                    listenEndClickFragment = new ListenEndClickFragment();
                }

                final AppSettings appSettings = new AppSettings(Tests.this);
                final Bundle bundle = appSettings.getTestFragmentState(ListenEndClickFragment.TAG);

                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.listen_end_click_fragment, listenEndClickFragment);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                transaction.addToBackStack(null);
                if (bundle.getInt(appSettings.KEY_COUNTER_RIGHT_ANSWER) > 0)
                {
                    DialogWarning dialogWarning = new DialogWarning();
                    Bundle dialogBundle = new Bundle();
                    dialogBundle.putString(dialogWarning.KEY_MESSAGE, getString(R.string.you_have_uncompleted_test));
                    dialogBundle.putString(dialogWarning.KEY_TEXT_OK_BUTTON, getString(R.string.text_continue));
                    dialogBundle.putString(dialogWarning.KEY_TEXT_NO_BUTTON, getString(R.string.text_from_the_beginning));
                    dialogBundle.putBoolean(dialogWarning.KEY_IS_NEUTRAL_BTN, true);
                    dialogWarning.setArguments(dialogBundle);
                    dialogWarning.setCancelable(false);
                    dialogWarning.setListener(new DialogWarning.IDialogResult()
                    {
                        @Override
                        public void dialogListener(boolean result)
                        {
                            if (result)
                            {
                                listenEndClickFragment.setArguments(appSettings.getTestFragmentState(ListenEndClickFragment.TAG));
                            }
                            else
                            {
                                appSettings.saveTestFragmentState(ListenEndClickFragment.TAG, null);
                                listenEndClickFragment.setArguments(null);
                            }
                            transaction.commit();
                        }
                    });
                    dialogWarning.show(getSupportFragmentManager(), dialogWarning.TAG);
                }
                else
                {
                    transaction.commit();
                }
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.t_tests_menu, menu);
//
//        return true;
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch (id)
        {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            getSupportFragmentManager().popBackStack();
        }
        else
        {
            finish();
        }
    }


}
