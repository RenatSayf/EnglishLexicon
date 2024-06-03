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
            context: Context
        ) {
            val localeRu = Locale("Ru", "ru")
            val startTimeInMillis = calculateLaunchTime()

            if (startTimeInMillis != null) {

                val workRequest = PeriodicWorkRequestBuilder<ResetDailyRewardWork>(Duration.ofDays(1)).apply {
                    setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .setRequiresDeviceIdle(true)
                            .build()
                    )

                    val currentTimeInMillis = Calendar.getInstance(localeRu).timeInMillis
                    val duration = Duration.ofMillis(startTimeInMillis.minus(currentTimeInMillis))
                    setInitialDelay(duration)
                }.build()
                WorkManager.getInstance(context)
                    .enqueueUniquePeriodicWork(workName, ExistingPeriodicWorkPolicy.KEEP, workRequest)
            } else {
                Exception("******* Wrong time ************").printStackTraceIfDebug()
            }
        }

        fun calculateLaunchTime(
            calendar: Calendar = Calendar.getInstance(Locale("RU", "ru")),
            startTime: String = "23:59 +0300"
        ): Long? {
            val timeFormat = SimpleDateFormat("HH:mm Z", Locale.getDefault())

            //val currentDate = Calendar.getInstance()

            return try {
                val startDate = timeFormat.parse(startTime)

                // Создаем календарь для парсенного времени
                calendar.time = startDate

                // Устанавливаем часы и минуты текущей даты по парсенному времени
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
                calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // Применяем часовой пояс
                val offset =
                    calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
                calendar.add(
                    Calendar.MILLISECOND,
                    offset - calendar.get(Calendar.ZONE_OFFSET) - calendar.get(Calendar.DST_OFFSET)
                )

                // Если установленное время меньше текущего, добавляем один день
                if (calendar.timeInMillis < Calendar.getInstance().timeInMillis) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                calendar.timeInMillis
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun doWork(): Result {

        context.showToast("****** ${ResetDailyRewardWork::class.java.simpleName}.doWork() has been done ********")
        return Result.success()
    }
}