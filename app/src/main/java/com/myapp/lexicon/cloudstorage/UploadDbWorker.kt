@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.cloudstorage

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myapp.lexicon.BuildConfig
import kotlinx.coroutines.delay


class UploadDbWorker(
    private val context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    companion object {

        val TAG = "${UploadDbWorker::class.java.simpleName}.tag5555"

        private var userId: String? = null

        private var listener: Listener? = null

        private val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
        }.build()

        private fun createWorkRequest(): OneTimeWorkRequest {

            return OneTimeWorkRequest.Builder(UploadDbWorker::class.java).apply {
                setConstraints(constraints)
                addTag(TAG)
            }.build()
        }

        fun uploadDbToCloud(context: Context, userId: String, listener: Listener): WorkManager {

            this.listener = listener
            val workManager = WorkManager.getInstance(context)
            this.userId = userId
            val workRequest = createWorkRequest()
            workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, workRequest)
            return workManager
        }
    }

    override suspend fun doWork(): Result {

        var result = Result.failure(Data.EMPTY)
        var isWorked = true

        userId?.let { id ->
            try {
                val databaseList = context.databaseList()
                val dbName = databaseList.first {
                    it == "lexicon_DB.db"
                }
                val databaseFile = context.getDatabasePath(dbName)
                val userFile = Uri.fromFile(databaseFile)

                if (BuildConfig.DEBUG) {
                    println("**************** databasePath: $userFile ******************")
                }

                val storageRef = Firebase.storage.reference
                val uploadTask = storageRef.child("/users/$id/${userFile.lastPathSegment}").putFile(userFile)

                uploadTask
                    .addOnSuccessListener { task ->
                        task.storage.downloadUrl.addOnSuccessListener { uri ->
                            listener?.onSuccess(uri)
                            result = Result.success()
                        }.addOnFailureListener { e ->
                            listener?.onFailure(e.message?: "****** Unknown error *******")
                        }.addOnCompleteListener {
                            isWorked = false
                            listener?.onComplete()
                        }
                    }
                    .addOnFailureListener { e ->
                        listener?.onFailure(e.message?: "****** Unknown error *******")
                        result = Result.failure()
                        isWorked = false
                    }
            } catch (e: Exception) {
                listener?.onFailure(e.message?: "****** Unknown error *******")
                result = Result.failure()
            }
        }?: run {
            listener?.onFailure("***** userId is null ********")
            result = Result.failure()
        }

        while (isWorked) {
            delay(100)
        }
        return result
    }

    interface Listener {
        fun onSuccess(uri: Uri)
        fun onFailure(error: String)
        fun onComplete()
    }
}

