package com.myapp.lexicon.webbrowser;

import android.content.Context;
import android.webkit.WebView;

public class LexiconWebView extends WebView
{
    public LexiconWebView(Context context)
    {
        super(context);
    }

    @Override
    public boolean performClick()
    {
        return true;
    }
}
