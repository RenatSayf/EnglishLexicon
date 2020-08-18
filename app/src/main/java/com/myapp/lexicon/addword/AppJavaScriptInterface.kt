package com.myapp.lexicon.addword

import android.webkit.JavascriptInterface
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.helpers.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

//todo parsing WebView: Step 2
class AppJavaScriptInterface
{
    companion object
    {
        //todo Отправка события в активити/фрагмент: Step 2
        val parseEvent : MutableLiveData<Event<String>> = MutableLiveData()
    }

    @JavascriptInterface
    fun handleHtml(html: String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                val doc = Jsoup.parse(html)
                val inputText = doc.select("#dictionary > li:nth-child(1) > div").text()
                parseEvent.value = Event(inputText) //todo Отправка события в активити/фрагмент: Step 3
                return@withContext
            }
        }
    }
}