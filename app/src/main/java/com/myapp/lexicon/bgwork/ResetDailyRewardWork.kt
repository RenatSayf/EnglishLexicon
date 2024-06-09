package com.myapp.lexicon.bgwork

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.myapp.lexicon.ads.RevenueViewModel
import com.myapp.lexicon.di.App
import com.myapp.lexicon.helpers.printStackTraceIfDebug
import com.myapp.lexicon.main.viewmodels.UserViewModel
import com.myapp.lexicon.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResetDailyRewardWork(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(appContext = context, params = params) {

    companion object {

        private val workName = "${ResetDailyRewardWork::class.java.simpleName}Dlk48785"
        fun enqueueAtTime(
            context: Context
        ) {

            val workRequest = OneTimeWorkRequestBuilder<ResetDailyRewardWork>().apply {
                setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresDeviceIdle(true)
                        .build()
                )
            }.build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, workRequest)
        }

    }

    override suspend fun doWork(): Result {

        return withContext(Dispatchers.IO) {

            val userVM = UserViewModel(App.INSTANCE)
            val revenueVM = RevenueViewModel(App.INSTANCE)

            val result = userVM.getUserFromCloud().value
            if (result?.isSuccess == true) {
                result.onSuccess { user: User ->
                    revenueVM.resetUserDailyReward(user)
                }
                Result.success()
            }
            else {
                result?.onFailure { exception: Throwable ->
                    exception.printStackTraceIfDebug()
                }
                Result.failure()
            }
        }
    }
}