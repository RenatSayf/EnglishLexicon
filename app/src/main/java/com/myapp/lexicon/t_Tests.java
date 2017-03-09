package com.myapp.lexicon;


import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.Locale;

public class t_Tests extends AppCompatActivity
{
    private ImageButton buttonFindPair, buttonListenEndClick, buttonOneOfFive;

    private t_FindPairFragment findPairFragment;

    private t_ListenEndClickFragment listenEndClickFragment;

    private t_OneOfFiveTest oneOfFiveTest;

    private FragmentTransaction transaction;
    private DataBaseQueries baseQueries;

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
        try
        {
            baseQueries = new DataBaseQueries(this);
        } catch (SQLException e)
        {
            e.printStackTrace();
            Toast.makeText(this,"Error - "+e.getMessage(),Toast.LENGTH_SHORT).show();
            this.finish();
        }

        buttonFindPair = (ImageButton) findViewById(R.id.btn_find_pair);
        buttonListenEndClick = (ImageButton) findViewById(R.id.btn_select_word_test);
        buttonOneOfFive = (ImageButton) findViewById(R.id.btn_test_1of5);

        FragmentManager manager = getSupportFragmentManager();

        String FIND_PAIR_FRAGMENT = "find_pair_fragment";
        findPairFragment = (t_FindPairFragment) manager.findFragmentByTag(FIND_PAIR_FRAGMENT);
        if (findPairFragment == null)
        {
            findPairFragment = new t_FindPairFragment();
            bundleFindPair = new Bundle();
        }

        String LISTEN_END_CLICK_FRAGMENT = "listenEndClickFragment";
        listenEndClickFragment = (t_ListenEndClickFragment) manager.findFragmentByTag(LISTEN_END_CLICK_FRAGMENT);
        if (listenEndClickFragment == null)
        {
            listenEndClickFragment = new t_ListenEndClickFragment();
            bundleListenTest = new Bundle();
        }

        String ONE_OF_FIVE_FRAGMENT = "one_of_five";
        oneOfFiveTest = (t_OneOfFiveTest) manager.findFragmentByTag(ONE_OF_FIVE_FRAGMENT);
        if (oneOfFiveTest == null)
        {
            oneOfFiveTest = new t_OneOfFiveTest();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id)
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_item1:
                break;
            case R.id.action_item2:
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
