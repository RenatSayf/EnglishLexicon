package com.myapp.lexicon.video.web

import android.os.CountDownTimer
import android.webkit.JavascriptInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.video.constants.VIDEO_URL
import com.myapp.lexicon.video.web.models.UrlHistoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

class YouTubeViewModel: ViewModel() {

    companion object {
        const val JS_TAG = "JS_TAG_25878945"
    }

    private val adShowInterval: Long = if (BuildConfig.DEBUG) {
        TimeUnit.SECONDS.toMillis(30)
    } else {
        val randomInterval: Int = (120..180).random()
        TimeUnit.SECONDS.toMillis(randomInterval.toLong())
    }

    private var timer: CountDownTimer? = null

    fun startAdTimer(millis: Long = adShowInterval) {
        if (timer == null) {
            timer = object : CountDownTimer(millis, millis) {
                override fun onTick(p0: Long) {
                    _timerState.value = TimerState.Start
                }

                override fun onFinish() {
                    _timerState.value = TimerState.Finish
                    timer = null
                }
            }.start()
        }
    }

    fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    private var _timerState = MutableLiveData<TimerState>(TimerState.Init)
    val timerState: LiveData<TimerState> = _timerState

    sealed class TimerState {
        data object Init: TimerState()
        data object Start: TimerState()
        data object Finish: TimerState()
    }

    fun parseIsPlayerPlay(
        rawHtml: String?,
        onStart: () -> Unit = {},
        onComplete: (ex: Exception?) -> Unit = {},
        onPlay: () -> Unit,
        onPause: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            run {
                onStart.invoke()
                try {
                    if (rawHtml != null) {
                        val htmlContent = prepareRawHtml(rawHtml)
                        val document = Jsoup.parse(htmlContent)

                        val element = withContext(Dispatchers.IO) {
                            document.getElementsByClass("player-control-play-pause-icon")
                        }
                        val attrD = element.first()?.firstElementChild()?.firstElementSibling()
                            ?.firstElementChild()?.firstElementSibling()
                            ?.firstElementChild()?.firstElementChild()
                            ?.firstElementChild()?.attr("d")

                        when(attrD) {
                            "m7 4 12 8-12 8V4z" -> {
                                onPause.invoke()
                            }
                            "M9 19H7V5h2Zm8-14h-2v14h2Z", null -> {
                                onPlay.invoke()
                            }
                        }
                    }
                }
                catch (e: IndexOutOfBoundsException) {
                    onPause.invoke()
                }
                catch (e: Exception) {
                    onComplete.invoke(e)
                }
                finally {
                    onComplete.invoke(null)
                }
            }
        }
    }

    @JavascriptInterface
    fun playPauseClickScript(): String {
        return """javascript:(
            |function(){
            |try {
            |document.getElementsByClassName('player-control-play-pause-icon')[0].click();
            |} catch (error) {}; 
            |try {
            |document.getElementsByClassName('touch-controls')[0].click();
            |} catch (error) {};
            |})()""".trimMargin()
    }

    private fun prepareRawHtml(rawHtml: String): String {
        return rawHtml.replace("\\u003C", "<").replace("\\", "")
    }

    val scriptGetHtmlContent: String = "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"

    val urlList: MutableSet<UrlHistoryItem> = mutableSetOf()

    fun getLastLoadedUrl(): String {
        val historyItem = urlList.toList().filter {
            it.url != VIDEO_URL
        }.maxByOrNull {
            it.time
        }
        return historyItem?.url ?: ""
    }

    sealed class NetworkState {
        data object Available: NetworkState()
        data object NotAvailable: NetworkState()
    }

    private var _networkState = MutableLiveData<NetworkState>(NetworkState.Available)
    val networkState: LiveData<NetworkState> = _networkState

    fun setNetworkState(state: NetworkState) {
        _networkState.value = state
    }

    override fun onCleared() {

        timer?.cancel()
        timer = null
        super.onCleared()
    }
}