package com.myapp.lexicon.wordstests;

import android.content.Context;
import android.widget.LinearLayout;

public class OverriddenPerformClickLinLayout extends LinearLayout
{
    public OverriddenPerformClickLinLayout(Context context)
    {
        super(context);
    }

    @Override
    public boolean performClick()
    {
        return super.performClick();
    }
}
