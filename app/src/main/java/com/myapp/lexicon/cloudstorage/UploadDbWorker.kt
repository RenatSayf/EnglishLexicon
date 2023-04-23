@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.cloudstorage

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myapp.lexicon.BuildConfig
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.checkCloudStorage
import com.myapp.lexicon.database.AppDataBase
import com.myapp.lexicon.settings.isRequireCloudSync
import kotlinx.coroutines.delay


@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class UploadDbWorker(
    private val context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params) {

    companion object {

        val TAG = "${UploadDbWorker::class.java.simpleName}.tag5555"

        private var userId: String? = null
        private var dbName: String? = null

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

        userId?.let { id ->

            context.checkCloudStorage(
                onFailure = { err ->
                    context.isRequireCloudSync = false
                    listener?.onFailure(err)
                    result = Result.failure()
                    isWorked = false
                },
                onNotRequireSync = {
                    context.isRequireCloudSync = false
                    listener?.onCanceled("******* Update is not require **************")
                    listener?.onComplete()
                    result = Result.success()
                    isWorked = false
                },
                onRequireSync = { bytes ->
                    try {
                        val databaseList = context.databaseList()
                        val dbName = databaseList.first {
                            it == dbName
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
                                    context.isRequireCloudSync = true
                                    listener?.onSuccess(uri)
                                    result = Result.success()
                                }.addOnFailureListener { e ->
                                    context.isRequireCloudSync = false
                                    listener?.onFailure(e.message?: "****** Unknown error *******")
                                }.addOnCompleteListener {
                                    isWorked = false
                                    listener?.onComplete()
                                }
                            }
                            .addOnFailureListener { e ->
                                context.isRequireCloudSync = false
                                listener?.onFailure(e.message?: "****** Unknown error *******")
                                result = Result.failure()
                                isWorked = false
                            }
                    } catch (e: Exception) {
                        context.isRequireCloudSync = false
                        listener?.onFailure(e.message?: "****** Unknown error *******")
                        result = Result.failure()
                    }
                }
            )
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
        fun onCanceled(message: String)
    }
}

