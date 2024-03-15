package com.myapp.lexicon.video.web

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.BuildConfig
import java.util.concurrent.TimeUnit

class YouTubeViewModel: ViewModel() {

    private val adShowInterval = if (BuildConfig.DEBUG) {
        TimeUnit.SECONDS.toMillis(30)
    } else {
        TimeUnit.MINUTES.toMillis(3)
    }

    private val timer = object : CountDownTimer(adShowInterval, adShowInterval) {
        override fun onTick(p0: Long) {
            _timerState.value = TimerState.Start
        }

        override fun onFinish() {
            _timerState.value = TimerState.Finish
        }
    }

    fun startAdTimer() {
        timer.start()
    }

    private var _timerState = MutableLiveData<TimerState>(TimerState.Init)
    val timerState: LiveData<TimerState> = _timerState

    sealed class TimerState {
        object Init: TimerState()
        object Start: TimerState()
        object Finish: TimerState()
    }

    override fun onCleared() {

        timer.cancel()
        super.onCleared()
    }
}