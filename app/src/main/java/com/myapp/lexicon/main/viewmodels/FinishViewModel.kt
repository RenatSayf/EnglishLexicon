package com.myapp.lexicon.main.viewmodels

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myapp.lexicon.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FinishViewModel : ViewModel() {

    private val millisInFuture: Long = if (BuildConfig.DEBUG) {
        TimeUnit.SECONDS.toMillis(30)
    }
    else {
        TimeUnit.SECONDS.toMillis(300)
    }
    private val countDownInterval: Long = TimeUnit.SECONDS.toMillis(30)

    private val timer = object : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(p0: Long) {}

        override fun onFinish() {
            viewModelScope.launch {
                _timeIsUp.emit(Result.success(true))
            }
        }
    }

    fun launchTimer() {
        timer.start()
    }

    fun cancelTimer() {
        timer.cancel()
    }

    private var _timeIsUp = MutableSharedFlow<Result<Boolean>>()
    val timeIsUp: SharedFlow<Result<Boolean>> = _timeIsUp
}