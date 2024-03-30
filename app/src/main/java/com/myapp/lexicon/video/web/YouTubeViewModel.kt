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

    private val adShowInterval = if (BuildConfig.DEBUG) {
        TimeUnit.SECONDS.toMillis(30)
    } else {
        val randomInterval = (120..180).random()
        TimeUnit.SECONDS.toMillis(randomInterval.toLong())
    }

    private var timer: CountDownTimer? = null

    fun startAdTimer() {
        if (timer == null) {
            timer = object : CountDownTimer(adShowInterval, adShowInterval) {
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
        object Init: TimerState()
        object Start: TimerState()
        object Finish: TimerState()
    }

    fun parseIsPlayerPlay(
        rawHtml: String?,
        onStart: () -> Unit = {},
        onComplete: (ex: Exception?) -> Unit = {},
        onPlay: () -> Unit,
        onPause: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                onStart.invoke()
                try {
                    val htmlContent = rawHtml?.replace("\\u003C", "<")?.replace("\\", "")
                    if (htmlContent != null) {
                        val document = Jsoup.parse(htmlContent)
                        val attr = document.getElementsByClass("player-control-play-pause-icon").firstOrNull()?.attr("aria-pressed")
                        if (attr == null) {
                            val element = document.getElementsByClass("is-scrubbable-mode").firstOrNull()
                            if (element == null) {
                                onPlay.invoke()
                            }
                            else {
                                onPause.invoke()
                            }
                        }
                        else {
                            if (attr == "true") {
                                onPlay.invoke()
                            }
                            else {
                                onPause.invoke()
                            }
                        }

                    } else {
                        onPause.invoke()
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
        object Available: NetworkState()
        object NotAvailable: NetworkState()
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