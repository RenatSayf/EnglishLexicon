package com.myapp.lexicon.cloudstorage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import kotlinx.android.synthetic.main.storage_fragment2.*

class StorageFragment2 : Fragment()
{

    companion object
    {
        fun newInstance() = StorageFragment2()
    }

    private lateinit var viewModel: StorageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.storage_fragment2, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StorageViewModel::class.java)

        uploadBtn.setOnClickListener {
            activity?.let {
                val taskResult = viewModel.createUpLoadTask(it)
                taskResult?.uploadTask?.addOnSuccessListener{
                    val taskUrl = taskResult.lexiconReference.downloadUrl
                    val uri = taskUrl.result.toString()
                    return@addOnSuccessListener
                }?.addOnFailureListener{ err ->
                    err.printStackTrace()
                    return@addOnFailureListener
                }
            }
        }


    }

}
