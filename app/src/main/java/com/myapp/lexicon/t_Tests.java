package com.myapp.lexicon;

//import android.app.Fragment;
//import android.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class t_Tests extends AppCompatActivity implements t_MatchFragment.OnFragmentInteractionListener
{
    private ImageButton buttonMatchTest, buttonSelectWordTest;
    private t_MatchFragment matchFragment;
    private FragmentTransaction transaction;


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
        buttonMatchTest = (ImageButton) findViewById(R.id.btn_match_test);
        buttonSelectWordTest = (ImageButton) findViewById(R.id.btn_select_word_test);
        matchFragment = t_MatchFragment.newInstance(null,null);
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
