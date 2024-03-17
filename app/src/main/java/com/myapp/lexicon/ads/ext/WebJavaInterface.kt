package com.myapp.lexicon.ads.ext

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import org.jsoup.Jsoup

class WebJavaInterface {

    companion object {
        const val JS_TAG = "JS_TAG_25878945"
    }

    @JavascriptInterface
    fun hideShowPlayerControls(flag: Boolean): String {
        val cssFlag = if (flag) "" else "none"

        val script = """javascript:(
            |function(){
            |let container = document.getElementById('player-control-container')
            |container.style.display = '$cssFlag';
            |})()""".trimMargin()

        return script
    }

    @JavascriptInterface
    fun isPlayerPlay(html: String): Boolean {
        return try {
            val document = Jsoup.parse(html)
            val value = document.getElementsByClass("yt-spec-icon-shape")[5]
                .getElementsByTag("div")[0]
                .getElementsByTag("svg")[0]
                .getElementsByTag("path")[0]
                .attribute("d").value
            value == "M9 19H7V5h2Zm8-14h-2v14h2Z"
        } catch (e: Exception) {
            e.printStackTraceIfDebug()
            false
        }
    }

    @JavascriptInterface
    fun performClickScript(): String {

        return "javascript:(function(){document.getElementsByClassName('player-control-play-pause-icon')[0].click();})()"
    }
}