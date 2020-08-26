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
                try
                {
                    val list : ArrayList<String> = arrayListOf()
                    val doc = Jsoup.parse(html)
                    doc.getElementById("shareLink").text().also {
                        if (!it.isNullOrEmpty())
                        {
                            val parsedInputText = parseInputText(it)
                            parsedInputText.isNotEmpty().let {
                                list.add(parsedInputText)
                            }
                        }
                    }
                    doc.select("#translation > span").text().also {
                        if (!it.isNullOrEmpty())
                        {
                            list.add(it)
                        }
                    }
                    val regex = Regex("[^a-zA-Z-0-9]")
                    if (list.size >= 2 && list.first().contains(regex) && !list.last().contains(regex))
                    {
                        val reversedList = list.reversed().distinct() as ArrayList<String>
                        parseEvent.value = Event(reversedList)
                        return@withContext
                    }
                    if (list.isNotEmpty())
                    {
                        val distinctList = list.distinct() as ArrayList<String>
                        parseEvent.value = Event(distinctList) //todo Отправка события в активити/фрагмент: Step 3
                    }
                    else
                    {
                        parseEvent.value = Event(arrayListOf())
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    parseEvent.value = Event(arrayListOf())
                }
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