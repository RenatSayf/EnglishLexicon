package com.myapp.lexicon.helpers;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Keyboard
{
    private static final Keyboard instance = new Keyboard();

    public static Keyboard getInstance()
    {
        return instance;
    }

    private Keyboard()
    {
    }

    public void forceHide(Context context, View view)
    {
        try
        {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException e)
        {
            e.printStackTrace();
        }
    }
}
