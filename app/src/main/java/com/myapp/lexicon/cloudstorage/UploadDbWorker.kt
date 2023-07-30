@file:Suppress("ObjectLiteralToLambda", "VARIABLE_WITH_REDUNDANT_INITIALIZER")

package com.myapp.lexicon.cloudstorage

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storage
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.helpers.getCRC32CheckSum
import kotlinx.coroutines.delay


class UploadDbWorker(
    private val context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    companion object {

        val TAG = "${UploadDbWorker::class.java.simpleName}.tag5555"

        private var dbName: String? = null
        private var userId: String = "1"

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

        fun uploadDbToCloud(
            context: Context,
            dbName: String = context.getString(R.string.data_base_name),
            userId: String,
            listener: Listener?
        ) {

            this.listener = listener
            val workManager = WorkManager.getInstance(context)
            this.dbName = dbName
            this.userId = userId
            val workRequest = createWorkRequest()
            workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, workRequest)
        }
    }

    override suspend fun doWork(): Result {

        var result = Result.failure(Data.EMPTY)
        var isWorked = true
        AppDataBase.dataBase?.close()

        try {
            val mainDbFile = context.getDatabasePath(dbName)
            val userFile = Uri.fromFile(mainDbFile)
            val checkSum = mainDbFile.readBytes().getCRC32CheckSum()

            if (BuildConfig.DEBUG) {
                println("************* ${UploadDbWorker::class.simpleName}.doWork() Database check sum: $checkSum ******************")
            }
            val storageRef = Firebase.storage.reference
            val customMetadata = StorageMetadata.Builder().apply {
                setCustomMetadata("CHECK_SUM", checkSum.toString())
            }.build()
            val uploadTask = storageRef.child("/users/$userId/${userFile.lastPathSegment}").putFile(userFile, customMetadata)

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
                }
                .addOnCompleteListener {
                    result = Result.failure()
                    isWorked = false
                    listener?.onComplete()
                }
        } catch (e: Exception) {
            listener?.onFailure(e.message?: "****** Unknown error *******")
            result = Result.failure()
        }
        finally {
            listener?.onComplete()
            isWorked = false
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
        fun onCanceled(message: String)
    }
}

