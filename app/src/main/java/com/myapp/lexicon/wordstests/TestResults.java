package com.myapp.lexicon.wordstests;

import android.content.Context;

import com.myapp.lexicon.R;

/**
 * Created by Ренат on 26.09.2016.
 */

public class TestResults
{
    private final Context context;

    TestResults(Context context)
    {
        this.context = context;
    }
    
    public String getOverallResult(float right, float total)
    {
        String result = context.getString(R.string.text_excellent);
        float precent;
        try
        {
            precent = (right / total) * 100;
        } catch (Exception e)
        {
            precent = 100;
        }
        if (precent == 100)
        {
            result = context.getString(R.string.text_excellent); 
        }
        else if (precent >= 70 && precent < 100)
        {
            result = context.getString(R.string.text_good);
        }
        else if (precent < 70)
        {
            result = context.getString(R.string.text_bad);
        }
        return result;
    }

}
