package com.myapp.lexicon;

/**
 * Created by Ренат on 26.09.2016.
 */

public class t_TestResults
{
    public String getOverallResult(int right, int total)
    {
        String result = "Отлично";
        int precent = 0;
        try
        {
            precent = (right / total) * 100;
        } catch (Exception e)
        {
            precent = 100;
        }
        if (precent == 100)
        {
            result = "Отлично";
        }
        else if (precent >= 70 && precent < 100)
        {
            result = "Хорошо";
        }
        else if (precent < 70)
        {
            result = "Плохо";
        }
        return result;
    }
}
