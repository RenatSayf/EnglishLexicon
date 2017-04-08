package com.myapp.lexicon.wordstests;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.myapp.lexicon.R;
import com.myapp.lexicon.database.GetTableListFragm;

public class Tests extends AppCompatActivity
{
    private ImageButton buttonFindPair, buttonListenEndClick, buttonOneOfFive;
    private FindPairFragment findPairFragment;
    private ListenEndClickFragment listenEndClickFragment;
    private OneOfFiveTest oneOfFiveTest;
    private FragmentTransaction transaction;

    public static String FIND_PAIR_FRAGMENT = "find_pair_fragment";
    public static String LISTEN_END_CLICK_FRAGMENT = "listen_and_click_fragment";
    public static String ONE_OF_FIVE_FRAGMENT = "one_of_five_fragment";

    public static Bundle bundleOneOfFiveTest;
    public static Bundle bundleListenTest;
    public static Bundle bundleFindPair;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.t_layout_tests);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    private void initViews()
    {
        buttonFindPair = (ImageButton) findViewById(R.id.btn_find_pair);
        buttonListenEndClick = (ImageButton) findViewById(R.id.btn_select_word_test);
        buttonOneOfFive = (ImageButton) findViewById(R.id.btn_test_1of5);

        FragmentManager manager = getSupportFragmentManager();

        findPairFragment = (FindPairFragment) manager.findFragmentByTag(FIND_PAIR_FRAGMENT);
        if (findPairFragment == null)
        {
            findPairFragment = new FindPairFragment();
            bundleFindPair = new Bundle();
        }

        listenEndClickFragment = (ListenEndClickFragment) manager.findFragmentByTag(LISTEN_END_CLICK_FRAGMENT);
        if (listenEndClickFragment == null)
        {
            listenEndClickFragment = new ListenEndClickFragment();
            bundleListenTest = new Bundle();
        }

        oneOfFiveTest = (OneOfFiveTest) manager.findFragmentByTag(ONE_OF_FIVE_FRAGMENT);
        if (oneOfFiveTest == null)
        {
            oneOfFiveTest = new OneOfFiveTest();
            bundleOneOfFiveTest = new Bundle();
        }

        button_OnClick();
    }

    private void button_OnClick()
    {
        buttonFindPair.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.find_pair_fragment, findPairFragment);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        buttonListenEndClick.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                transaction = getSupportFragmentManager().beginTransaction();
                //transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.listen_end_click_fragment, listenEndClickFragment);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                //transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        buttonOneOfFive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                transaction = getSupportFragmentManager().beginTransaction();
                //transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_1of5, oneOfFiveTest);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                //transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);
                transaction.addToBackStack(null);
                transaction.commit();
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
            bundleOneOfFiveTest = null;
            bundleListenTest = null;
            finish();
        }
    }


}
