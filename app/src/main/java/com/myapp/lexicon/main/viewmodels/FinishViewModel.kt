package com.myapp.lexicon.main.viewmodels

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.helpers.printLogIfDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FinishViewModel : ViewModel() {

    private val millisInFuture: Long = if (BuildConfig.DEBUG) {
        TimeUnit.SECONDS.toMillis(30)
    }
    else {
        TimeUnit.SECONDS.toMillis(180)
    }
    private var job: Job? = null
    private val countDownInterval: Long = TimeUnit.SECONDS.toMillis(30)

    private val timer = object : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(p0: Long) {}

        override fun onFinish() {
            printLogIfDebug("************ CountDownTimer finish **************")
            //region: If use the viewmodelscope, then this code snippet will only work once. For some reason...
            job = CoroutineScope(Dispatchers.Main).launch {
                _timeIsUp.emit(Result.success(true))
            }
            //endregion
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

    override fun onCleared() {

        job?.cancel()
        super.onCleared()
    }

}