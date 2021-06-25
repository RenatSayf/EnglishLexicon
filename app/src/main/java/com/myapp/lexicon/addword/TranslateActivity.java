package com.myapp.lexicon.addword;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.myapp.lexicon.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class TranslateActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_translate_activity);



        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        String enWord;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            Intent intent = getIntent();
            if (intent != null)
            {
                CharSequence sequence = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
                if (sequence != null)
                {
                    enWord = Objects.requireNonNull(intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)).toString().toLowerCase();
                    TranslateFragment translateFragment = TranslateFragment.Companion.getInstance(enWord);
                    getSupportFragmentManager().beginTransaction().add(R.id.translate_fragment, translateFragment).addToBackStack(null).commit();
                }
            }
        }


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        finish();
        super.onBackPressed();
    }
}
