package com.myapp.lexicon;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by Ренат on 16.12.2016.
 */

public class t_TextArrayHelper
{
    private ArrayList<String> textArray;

    public t_TextArrayHelper(RelativeLayout layout)
    {
        textArray = new ArrayList<>();
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            try
            {
                Button button = (Button) layout.getChildAt(i);
                textArray.add(button.getText().toString());
            } catch (Exception e)
            {
                z_Log.v(e.getMessage());
            }
        }
    }

    public void updateArray(int index, String text)
    {
        if (index == 0 && !text.equals(""))
        {
            textArray.set(0, text);
        }
        if (index > 0)
        {
            for (int i = index; i > 0; i--)
            {
                textArray.set(i, textArray.get(i-1));
            }
            textArray.set(0, null);
        }
        return;
    }

    public ArrayList<String> getCurrentArray()
    {
        return textArray;
    }

    public void clear()
    {
        textArray.clear();
    }
}
