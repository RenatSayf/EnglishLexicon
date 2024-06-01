package com.myapp.lexicon.bgwork

import android.app.PendingIntent
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration


fun Context.resetDailyReward() {
    val workRequest = PeriodicWorkRequestBuilder<ResetDailyRewardWork>(Duration.ofDays(1)).apply {
        setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
    }.build()
    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork("XXXXXX", ExistingPeriodicWorkPolicy.KEEP, workRequest)
}
//fun Context.createResetIntent(): PendingIntent {
//
//}
