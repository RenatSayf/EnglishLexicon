package com.myapp.lexicon.addword

import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import com.myapp.lexicon.R

//todo parsing WebView: Step 1
class AppWebViewClient constructor(private val root: TranslateFragment) : WebViewClient()
{
    override fun onPageFinished(view: WebView?, url: String?)
    {
        super.onPageFinished(view, url)
        root.view?.findViewById<Button>(R.id.btnSave)?.let {
            it.visibility = View.VISIBLE
        }

        root.view?.findViewById<ProgressBar>(R.id.loadProgress)?.let {
            it.visibility = View.GONE
        }
        //root.btnSave?.let { it.visibility = View.VISIBLE }
        //root.loadProgress?.let { it.visibility = View.GONE }
        return
    }

}