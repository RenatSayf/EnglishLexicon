package com.myapp.lexicon.cloudstorage

import android.content.Context
import androidx.work.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.getCloudDbRefFromPref
import kotlinx.coroutines.delay


class DownloadDbWorker(
    private val context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    companion object {

        val TAG = "${DownloadDbWorker::class.java.simpleName}.tag3333"

        private var userId: String? = null

        private var listener: Listener? = null

        private const val ONE_MEGABYTE: Long = 1024 * 1024

        private val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
        }.build()

        private fun createWorkRequest(): OneTimeWorkRequest {

            return OneTimeWorkRequest.Builder(DownloadDbWorker::class.java).apply {
                setConstraints(constraints)
                addTag(TAG)
            }.build()
        }

        fun downloadDbFromCloud(context: Context, userId: String?, listener: Listener?) {

            this.listener = listener
            if (userId.isNullOrEmpty()) {
                context.getCloudDbRefFromPref {
                    this.userId = it
                }
            } else this.userId = userId
            val workManager = WorkManager.getInstance(context)
            val workRequest = createWorkRequest()
            workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, workRequest)
        }
    }

    override suspend fun doWork(): Result {

        var result = Result.failure(Data.EMPTY)
        var isWorked = true
        val dbName = context.getString(R.string.data_base_name)

        userId?.let { id ->

            val storageRef = Firebase.storage.reference.child("/users/$id/$dbName")

            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
                result = try {
//                    val output = context.openFileOutput(dbName, Context.MODE_PRIVATE)
//                    output.write(bytes)
                    listener?.onSuccess(bytes)
                    Result.success()
                } catch (e: Exception) {
                    listener?.onFailure(e.message?: "File not found")
                    Result.failure()
                } finally {
                    isWorked = false
                }
            }.addOnFailureListener { e ->
                listener?.onFailure(e.message?: "Unknown error")
                result = Result.failure()
            }.addOnCompleteListener {
                listener?.onComplete()

            }
        }?: run {
            listener?.onFailure("********* dbRemoteRef is null *********")
            result = Result.failure()
            isWorked = false
        }
        while (isWorked) {
            delay(100)
        }
        return result
    }

    interface Listener {
        fun onSuccess(bytes: ByteArray)
        fun onFailure(error: String)
        fun onComplete()
    }
}