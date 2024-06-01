package com.myapp.lexicon.bgwork

import android.content.Context
import android.icu.util.Calendar
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showToast
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.Locale

class ResetDailyRewardWork(
    private val context: Context,
    params: WorkerParameters
): CoroutineWorker(appContext = context, params = params) {

    companion object {

        private val workName = "${ResetDailyRewardWork::class.java.simpleName}Dlk48785"
        fun enqueueAtTime(
            context: Context,
            startTime: String = "23:59 +0300"
        ) {
            val currentTimeInMillis = Calendar.getInstance(Locale.getDefault()).timeInMillis
            val dateFormat = SimpleDateFormat("HH:mm Z", Locale.getDefault())
            val startTimeInMillis = dateFormat.parse(startTime)?.time?.plus(currentTimeInMillis)

            if (startTimeInMillis != null && startTimeInMillis > currentTimeInMillis) {

                val duration = Duration.ofMillis(startTimeInMillis - currentTimeInMillis)
                val hours = duration.toHours()

                val workRequest = PeriodicWorkRequestBuilder<ResetDailyRewardWork>(Duration.ofDays(1)).apply {
                    setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    setInitialDelay(
                        Duration.ofMillis(startTimeInMillis - currentTimeInMillis)
                    )
                }.build()
                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(workName,
                        ExistingPeriodicWorkPolicy.KEEP, workRequest)
            } else {
                Exception("******* Wrong time ************").printStackTraceIfDebug()
            }
        }
    }
    override suspend fun doWork(): Result {

        context.showToast("****** ${ResetDailyRewardWork::class.java.simpleName}.doWork() has been done ********")
        return Result.success()
    }
}