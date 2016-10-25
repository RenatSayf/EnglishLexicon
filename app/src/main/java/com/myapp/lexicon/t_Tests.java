package com.myapp.lexicon;

//import android.app.Fragment;
//import android.app.FragmentTransaction;
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

public class t_Tests extends AppCompatActivity implements t_MatchFragment.OnFragmentInteractionListener
{
    private ImageButton buttonMatchTest, buttonSelectWordTest, buttonTest3, buttonOneOfFive;
    private t_MatchFragment matchFragment;
    private static String MATCH_FRAGMENT = "matchFragment";
    private t_ListenEndClickFragment listenEndClickFragment;
    private String LISTEN_END_CLICK_FRAGMENT = "listenEndClickFragment";
    private t_WriteWordFragment correctFragment2;
    private String CORRECT_FRAGMENT2 = "correctFragment2";
    private t_OneOfFiveTest oneOfFiveTest;
    private static String ONE_OF_FIVE_FRAGMENT = "one_of_five";
    private FragmentTransaction transaction;
    private DataBaseQueries baseQueries;


    private TextToSpeech speech;
    public static t_DialogTestComplete dialogTestComplete = new t_DialogTestComplete();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.t_layout_tests);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        initViews();

        String language = a_SplashScreenActivity.speech.getLanguage().getDisplayLanguage();
        String languageENG = Locale.US.getDisplayLanguage();
        if (language != languageENG)
        {
            while (!language.equals(languageENG))
            {
                a_SplashScreenActivity.speech.setLanguage(Locale.US);
                language = a_SplashScreenActivity.speech.getLanguage().getDisplayLanguage();
            }
        }
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

        buttonMatchTest = (ImageButton) findViewById(R.id.btn_match_test);
        buttonSelectWordTest = (ImageButton) findViewById(R.id.btn_select_word_test);
        buttonTest3 = (ImageButton) findViewById(R.id.btn_test_3);
        buttonOneOfFive = (ImageButton) findViewById(R.id.btn_test_1of5);

        FragmentManager manager = getSupportFragmentManager();
        matchFragment = (t_MatchFragment) manager.findFragmentByTag(MATCH_FRAGMENT);
        if (matchFragment == null)
        {
            matchFragment = t_MatchFragment.newInstance(null,null);
        }

        listenEndClickFragment = (t_ListenEndClickFragment) manager.findFragmentByTag(LISTEN_END_CLICK_FRAGMENT);
        if (listenEndClickFragment == null)
        {
            listenEndClickFragment = new t_ListenEndClickFragment();
        }

        correctFragment2 = (t_WriteWordFragment) manager.findFragmentByTag(CORRECT_FRAGMENT2);
        if (correctFragment2 == null)
        {
            correctFragment2 = new t_WriteWordFragment();
        }

        oneOfFiveTest = (t_OneOfFiveTest) manager.findFragmentByTag(ONE_OF_FIVE_FRAGMENT);
        if (oneOfFiveTest == null)
        {
            oneOfFiveTest = new t_OneOfFiveTest();
        }

        button_OnClick();
    }

    private void button_OnClick()
    {
        buttonMatchTest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                transaction = getSupportFragmentManager().beginTransaction();
                //transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.match_fragment, matchFragment);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                //transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_in_right);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        buttonSelectWordTest.setOnClickListener(new View.OnClickListener()
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

        buttonTest3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                transaction = getSupportFragmentManager().beginTransaction();
                //transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.write_word_fragment, correctFragment2);
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

    @Override
    public void onFragmentInteraction(Uri uri)
    {

    }


    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
        else
            finish();
    }

}
