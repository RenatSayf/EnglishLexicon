package com.myapp.lexicon.addword;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;

import com.myapp.lexicon.R;
import com.myapp.lexicon.dialogs.TranslatorDialog;

public class TranslateActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_translate_activity);
        String enWord = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            enWord = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
            TranslatorDialog translatorDialog = TranslatorDialog.getInstance(enWord, new TranslatorDialog.NoticeDialogListener()
            {
                @Override
                public void onDialogAddClick(AppCompatDialogFragment dialog)
                {
                    finish();
                }

                @Override
                public void onDialogCancelClick(AppCompatDialogFragment dialog)
                {
                    finish();
                }
            });
            FragmentManager fragmentManager = getSupportFragmentManager();
            translatorDialog.show(fragmentManager,TranslatorDialog.TAG);
        }


    }
}
