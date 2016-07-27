package com.myapp.lexicon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

public class t_Tests extends AppCompatActivity
{
    private ImageButton buttonMatchTest, buttonSelectWordTest;


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
        button_OnClick();
    }

    private void button_OnClick()
    {
        buttonMatchTest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_item1)
        {
            return true;
        }

        switch (id)
        {
            case R.id.action_item1:
                break;
            case R.id.action_item2:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

}
