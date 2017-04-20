package com.myapp.lexicon.main;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterViewFlipper;

/**
 * TODO: AdapterViewFlipper: 3. Класс-костыль для устранения IllegalArgumentException: Receiver not registered
 */

public class MyAdapterViewFlipper extends AdapterViewFlipper
{
    public MyAdapterViewFlipper(Context context)
    {
        super(context);
    }

    public MyAdapterViewFlipper(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override // TODO: AdapterViewFlipper: 4. Переопределение метода
    protected void onDetachedFromWindow()
    {
        try
        {
            super.onDetachedFromWindow();
        }
        catch (IllegalArgumentException e)
        {
            stopFlipping();
        }
    }
}
