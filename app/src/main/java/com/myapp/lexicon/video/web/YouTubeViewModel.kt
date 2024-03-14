package com.myapp.lexicon.video.web

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.TimeUnit

class YouTubeViewModel: ViewModel() {

    var canAdShow = true
    var adIsLoaded = false

    private var timeInMillis = TimeUnit.MINUTES.toMillis(1)

    private val timer = object : CountDownTimer(timeInMillis, timeInMillis) {
        override fun onTick(p0: Long) {
            canAdShow = false
            _timerState.value = TimerState.Start
        }

        override fun onFinish() {
            canAdShow = true
            _timerState.value = TimerState.Finish
        }
    }

    fun startAdTimer(timeInMillis: Long) {
        this.timeInMillis = timeInMillis
        timer.start()
    }

    private var _timerState = MutableLiveData<TimerState>(TimerState.Init)
    val timerState: LiveData<TimerState> = _timerState

    sealed class TimerState {
        object Init: TimerState()
        object Start: TimerState()
        object Finish: TimerState()
    }
}