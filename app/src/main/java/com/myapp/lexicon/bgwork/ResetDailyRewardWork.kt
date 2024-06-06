package com.myapp.lexicon.bgwork

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.TimeZone
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.myapp.lexicon.di.App
import com.myapp.lexicon.helpers.logIfDebug
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.helpers.showToast
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            context: Context
        ) {
            val duration = calculateInitialDelay(endTimeStr = "19:50 +0000")

            if (duration != null) {

                val workRequest = PeriodicWorkRequestBuilder<ResetDailyRewardWork>(Duration.ofDays(1)).apply {
                    setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .setRequiresDeviceIdle(true)
                            .build()
                    )
                    setInitialDelay(duration)
                }.build()
                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, workRequest)
            } else {
                Exception("******* Wrong time ************").printStackTraceIfDebug()
            }
        }

        fun calculateInitialDelay(
            currentTimeInMillis: Long = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).timeInMillis,
            endTimeStr: String = "23:59 +0000"
        ): Duration? {
            val timeFormat = SimpleDateFormat("HH:mm Z", Locale.getDefault())

            return try {

                val targetTimeCalendar = Calendar.getInstance().apply {
                    timeZone = TimeZone.getTimeZone("Europe/Moscow")
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val offset = targetTimeCalendar.get(Calendar.ZONE_OFFSET) + targetTimeCalendar.get(Calendar.DST_OFFSET)
                targetTimeCalendar.add(
                    Calendar.MILLISECOND,
                    offset - targetTimeCalendar.get(Calendar.ZONE_OFFSET) - targetTimeCalendar.get(Calendar.DST_OFFSET)
                )

                if (targetTimeCalendar.timeInMillis < currentTimeInMillis) {
                    targetTimeCalendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val endDate = timeFormat.parse(endTimeStr)
                if (endDate != null) {
                    targetTimeCalendar.timeInMillis += endDate.time
                    Duration.ofMillis(targetTimeCalendar.timeInMillis - currentTimeInMillis)
                }
                else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun doWork(): Result {

        return withContext(Dispatchers.IO) {

            val userVM = UserViewModel(App.INSTANCE)

            val result = userVM.getUserFromCloud().value
            result?.onSuccess { value: User ->
                value.toString().logIfDebug()
            }
            result?.onFailure { exception: Throwable ->
                exception.printStackTraceIfDebug()
            }

            context.showToast("****** ${ResetDailyRewardWork::class.java.simpleName}.doWork() has been done ********")
            Result.success()
        }
    }
}