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
        val parseEvent : MutableLiveData<Event<ArrayList<String>>> = MutableLiveData()
    }

    @JavascriptInterface
    fun handleHtml(html: String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main){
                val doc = Jsoup.parse(html)
                val list = ArrayList<String>()
                val dictionary = doc.select("#dictionary > li:nth-child(1) > div")
                if (dictionary.size > 0)
                {
                    val inputText: String? = dictionary[0].ownText()
                    if (inputText != null)
                    {
                        list.add(inputText)
                    }
                }
                else
                {
                    parseEvent.value = Event(list)
                    return@withContext
                }

                val translate: String? = doc.select("#translation > span").text()
                translate?.let { list.add(it) }
                val ul = doc.getElementById("dictionary")
                ul.children().forEach {
                    val textHtml: String? = it.select("ol > li:nth-child(1) > div.dictionary-meanings-value > span:nth-child(1)").text()
                    textHtml?.let { txt -> list.add(txt) }
                }
                val regex = Regex("[^a-zA-Z]")
                if (list.size >= 2 && list.first().contains(regex) && !list.last().contains(regex))
                {
                    val reversedList = list.reversed().distinct() as ArrayList<String>
                    parseEvent.value = Event(reversedList)
                    return@withContext
                }
                val distinctList = list.distinct() as ArrayList<String>
                parseEvent.value = Event(distinctList) //todo Отправка события в активити/фрагмент: Step 3
                return@withContext
            }
        }
    }
}