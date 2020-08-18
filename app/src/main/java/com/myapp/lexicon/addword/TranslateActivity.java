package com.myapp.lexicon.addword;

import android.content.Intent;
import android.os.Bundle;

import com.myapp.lexicon.R;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;

public class TranslateActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_translate_activity);
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
//                    TranslatorDialog translatorDialog = TranslatorDialog.getInstance(enWord, new TranslatorDialog.NoticeDialogListener()
//                    {
//                        @Override
//                        public void onDialogAddClick(AppCompatDialogFragment dialog)
//                        {
//                            finish();
//                        }
//
//                        @Override
//                        public void onDialogCancelClick(AppCompatDialogFragment dialog)
//                        {
//                            finish();
//                        }
//                    });
//                    FragmentManager fragmentManager = getSupportFragmentManager();
//                    translatorDialog.show(fragmentManager,TranslatorDialog.TAG);
                }
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
        super.onBackPressed();
    }
}
