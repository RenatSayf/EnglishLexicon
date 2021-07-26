package com.myapp.lexicon.cloudstorage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.StorageFragment2Binding

class StorageFragment2 : Fragment(R.layout.storage_fragment2)
{
    companion object
    {
        fun newInstance() = StorageFragment2()
    }

    private lateinit var binding: StorageFragment2Binding
    private lateinit var viewModel: StorageViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.storage_fragment2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding = StorageFragment2Binding.bind(view)

        viewModel = ViewModelProvider(this).get(StorageViewModel::class.java)

        binding.uploadBtn.setOnClickListener {
            activity?.let {
                val taskResult = viewModel.createUpLoadTask(it)
                taskResult?.uploadTask?.addOnSuccessListener{
                    val taskUrl = taskResult.lexiconReference.downloadUrl
                    taskUrl.addOnSuccessListener { ur ->
                        ur.toString()
                        //return@addOnSuccessListener
                    }
                }?.addOnFailureListener{ err ->
                    err.printStackTrace()
                    return@addOnFailureListener
                }
            }
        }

    }

}
