package com.myapp.lexicon.cloudstorage

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.myapp.lexicon.database.DatabaseHelper
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class StorageViewModel : ViewModel()
{
    data class TaskResult(val uploadTask: UploadTask, val lexiconReference: StorageReference)

    init
    {

    }

    fun createUpLoadTask(context: Context) : TaskResult?
    {
        val firebaseStorage = FirebaseStorage.getInstance()
        val storageReference = firebaseStorage.reference
        val lexiconRef = storageReference.child("lexicon")
        val filePath: String = DatabaseHelper(context).filePath
        return try
        {
            val inputStream = FileInputStream(File(filePath))
            val uploadTask = lexiconRef.putStream(inputStream)
            TaskResult(uploadTask, lexiconRef)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            null
        }
    }
}
