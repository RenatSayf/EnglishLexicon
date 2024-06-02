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
import com.myapp.lexicon.helpers.toStringTime
import io.ktor.util.date.getTimeMillis
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
            startTime: String = "02:59 +0300"
        ) {
            val localeRu = Locale("ru", "RU")
            val calendar = Calendar.getInstance(Locale.getDefault())
            val currentTimeInMillis = calendar.timeInMillis
            val currentStrTime = currentTimeInMillis.toStringTime()

            val dateFormat = SimpleDateFormat("HH:mm Z", localeRu)
            val startTimeInMillis = getTimeInMillis(startTime)//dateFormat.parse(startTime)?.time
            val startTimeStr = startTimeInMillis?.toStringTime()

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

        fun getTimeInMillis(timeString: String): Long {
            // Формат времени
            val timeFormat = SimpleDateFormat("HH:mm Z", Locale.getDefault())

            // Текущая дата
            val currentDate = Calendar.getInstance()

            return try {
                // Парсим время из строки
                val time = timeFormat.parse(timeString)

                // Создаем календарь для парсенного времени
                val timeCalendar = Calendar.getInstance()
                timeCalendar.time = time

                // Устанавливаем часы и минуты текущей даты по парсенному времени
                currentDate.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                currentDate.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                currentDate.set(Calendar.SECOND, 0)
                currentDate.set(Calendar.MILLISECOND, 0)

                // Применяем часовой пояс
                val offset = timeCalendar.get(Calendar.ZONE_OFFSET) + timeCalendar.get(Calendar.DST_OFFSET)
                currentDate.add(Calendar.MILLISECOND, offset - currentDate.get(Calendar.ZONE_OFFSET) - currentDate.get(Calendar.DST_OFFSET))

                // Если установленное время меньше текущего, добавляем один день
                if (currentDate.timeInMillis < Calendar.getInstance().timeInMillis) {
                    currentDate.add(Calendar.DAY_OF_YEAR, 1)
                }

                currentDate.timeInMillis
            } catch (e: Exception) {
                e.printStackTrace()
                throw IllegalArgumentException("Ошибка при парсинге времени: ${e.message}")
            }
        }
    }

    override suspend fun doWork(): Result {

        context.showToast("****** ${ResetDailyRewardWork::class.java.simpleName}.doWork() has been done ********")
        return Result.success()
    }
}