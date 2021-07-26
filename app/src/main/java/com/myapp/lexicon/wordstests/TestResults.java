package com.myapp.lexicon.wordstests;

import android.content.Context;

import com.myapp.lexicon.R;

/**
 * Created by Ренат on 26.09.2016.
 */

class TestResults
{
    private final Context context;

    TestResults(Context context)
    {
        this.context = context;
    }
    
    String getOverallResult(float right, float total)
    {
        String result = context.getString(R.string.text_excellent);
        float percent;
        try
        {
            percent = (right / total) * 100;
        } catch (Exception e)
        {
            percent = 100;
        }
        if (percent == 100)
        {
            result = context.getString(R.string.text_excellent); 
        }
        else if (percent >= 70 && percent < 100)
        {
            result = context.getString(R.string.text_good);
        }
        else if (percent < 70)
        {
            result = context.getString(R.string.text_bad);
        }
        return result;
    }

}
