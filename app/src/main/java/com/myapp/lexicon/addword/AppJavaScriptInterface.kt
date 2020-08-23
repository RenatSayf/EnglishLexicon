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
                val list : ArrayList<String> = arrayListOf()
                val doc = Jsoup.parse(html)
                val shareLink = doc.getElementById("shareLink").text()
                val parsedInputText = parseInputText(shareLink)
                parsedInputText.isNotEmpty().let {
                    list.add(parsedInputText)
                }
                val translate: String = doc.select("#translation > span").text()
                list.add(translate)
                val regex = Regex("[^a-zA-Z-0-9]")
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

    private fun parseInputText(link: String) : String
    {
        return kotlin.run {
            val substringAfter = link.substringAfter("text=")
            val substringBefore = substringAfter.substringBefore("</a>")
            val text = substringBefore.replace("+", " ")
            text
        }
    }
}