package com.myapp.lexicon.addword

import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.translate_fragment.*

//todo parsing WebView: Step 1
class AppWebViewClient constructor(private val root: TranslateFragment) : WebViewClient()
{
    override fun onPageFinished(view: WebView?, url: String?)
    {
        super.onPageFinished(view, url)
        root.btnSave?.let { it.visibility = View.VISIBLE }
        root.loadProgress?.let { it.visibility = View.GONE }
        return
    }
}