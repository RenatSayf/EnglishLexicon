package com.myapp.lexicon;

//import android.app.Fragment;
//import android.app.FragmentTransaction;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.util.Locale;

public class t_Tests extends AppCompatActivity implements t_MatchFragment.OnFragmentInteractionListener
{
    private ImageButton buttonMatchTest, buttonSelectWordTest;
    private t_MatchFragment matchFragment;
    private static String FRAGMENT_INSTANCE_NAME = "matchFragment";
    private FragmentTransaction transaction;
    private TextToSpeech speech;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.t_layout_tests);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();

        speech = new TextToSpeech(this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status == TextToSpeech.SUCCESS)
                {
                    int resultEn = speech.isLanguageAvailable(Locale.UK);
                    if (resultEn != TextToSpeech.LANG_MISSING_DATA || resultEn != TextToSpeech.LANG_NOT_SUPPORTED)
                    {
                        Log.i("Lexicon", "Извините, английский язык не поддерживается");
                        speech.setLanguage(Locale.US);

                        speech.speak("go", TextToSpeech.QUEUE_ADD, null);
                    }
                } else
                {
                    Log.i("Lexicon", "z_speechSynthesAsync.onInit() - Ошибка!");
                }
            }
        });
    }

    private void initViews()
    {
        buttonMatchTest = (ImageButton) findViewById(R.id.btn_match_test);
        buttonSelectWordTest = (ImageButton) findViewById(R.id.btn_select_word_test);

        FragmentManager manager = getSupportFragmentManager();
        matchFragment = (t_MatchFragment) manager.findFragmentByTag(FRAGMENT_INSTANCE_NAME);
        if (matchFragment == null)
        {
            matchFragment = t_MatchFragment.newInstance(null,null);
        }
        //matchFragment.setRetainInstance(true);
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

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.t_tests_menu, menu);

        return true;
    }
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
