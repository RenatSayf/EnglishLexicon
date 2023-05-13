package com.myapp.lexicon.cloudstorage

import android.content.Context
import androidx.work.*
import com.myapp.lexicon.R
import com.myapp.lexicon.settings.checkCloudStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


class CloudCheckWorker(
    context: Context,
    parameters: WorkerParameters
): CoroutineWorker(context, parameters) {

    companion object {
        val TAG = "${CloudCheckWorker::class.java.simpleName}.tag11111"

        private var dbName: String? = null

        private var listener:Listener? = null

        private val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
        }.build()

        private fun createWorkRequest(): OneTimeWorkRequest {

            return OneTimeWorkRequest.Builder(CloudCheckWorker::class.java).apply {
                setConstraints(constraints)
                addTag(TAG)
            }.build()
        }

        fun check(
            context: Context,
            dbName: String = context.getString(R.string.data_base_name),
            listener: Listener? = null
        ) {

            this.listener = listener
            val workManager = WorkManager.getInstance(context)
            this.dbName = dbName
            val workRequest = createWorkRequest()
            workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {

            try {
                var isWorked = true
                val context = this@CloudCheckWorker.applicationContext
                context.checkCloudStorage(
                    dbName = dbName?: "",
                    onRequireDownSync = { token ->
                        listener?.onRequireDownSync(token)
                        isWorked = false
                    },
                    onRequireUpSync = { token ->
                        if (dbName == context.getString(R.string.data_base_name)) {
                            UploadDbWorker.uploadDbToCloud(context, dbName!!, token, null)
                        }
                        else {
                            listener?.onRequireUpSync(token)
                        }
                        isWorked = false
                    },
                    onNotRequireSync = {
                        listener?.onNotRequireSync()
                        isWorked = false
                    }
                )

                while (isWorked) {
                    delay(100)
                }
                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    interface Listener {
        fun onRequireUpSync(token: String)
        fun onRequireDownSync(token: String)
        fun onNotRequireSync()
    }
}