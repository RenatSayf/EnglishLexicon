package com.myapp.lexicon.webbrowser;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.webkit.WebView;

public class LexiconWebView extends WebView
{
    public LexiconWebView(Context context)
    {
        super(context);
    }

    public LexiconWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LexiconWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LexiconWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean performClick()
    {
        super.performClick();
        return true;
    }
}
