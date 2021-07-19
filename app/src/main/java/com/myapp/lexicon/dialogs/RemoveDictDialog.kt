package com.myapp.lexicon.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.main.MainViewModel
import com.myapp.lexicon.repository.DataRepositoryImpl
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class RemoveDictDialog : DialogFragment()
{
    private lateinit var viewModel: MainViewModel
    private var subscribe: Disposable? = null

    @Inject
    lateinit var repository: DataRepositoryImpl

    @JvmField
    var dictsIsRemoved = MutableLiveData(false)

    companion object
    {
        const val TAG = "remove_dict_dialog"
        const val ARG_INPUT = "arg_input_list"
        private var instance: RemoveDictDialog? = null
        private lateinit var dialogCallback: IRemoveDictDialogCallback

        fun getInstance(list: ArrayList<String>, callback: IRemoveDictDialogCallback) : RemoveDictDialog = if (instance == null)
        {
            dialogCallback = callback
            RemoveDictDialog().apply {
                arguments = Bundle().apply {
                    val array : Array<out String> = list.toArray(emptyArray())
                    putStringArray(ARG_INPUT, array)
                }
            }
        }
        else instance as RemoveDictDialog
    }

    interface IRemoveDictDialogCallback
    {
        fun removeDictDialogButtonClickListener(list: MutableList<String>)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val inputArray: Array<String> = arguments?.getStringArray(ARG_INPUT) as Array<String>
        val deleteItems = ArrayList<String>()
        val choice = BooleanArray(inputArray.size)

        return AlertDialog.Builder(requireContext()).setTitle(R.string.title_del_dict)
               .setMultiChoiceItems(inputArray, choice) { dialog, which, isChecked -> deleteItems.add(inputArray[which]) }
               .setPositiveButton(R.string.button_text_delete) { dialog, which ->

                    if (deleteItems.size <= 0) return@setPositiveButton
                    AlertDialog.Builder(requireContext()).setTitle(R.string.dialog_are_you_sure)
                            .setPositiveButton(R.string.button_text_yes) { dialog1: DialogInterface?, which1: Int ->

                                deleteItems.forEachIndexed { index, item ->
                                    subscribe = viewModel.deleteDict(item)
                                            .subscribeOn(Schedulers.newThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                if (index == deleteItems.size - 1)
                                                {
                                                    Toast.makeText(requireContext(), R.string.msg_selected_dict_removed, Toast.LENGTH_LONG).show()
                                                    dialogCallback.removeDictDialogButtonClickListener(deleteItems)
                                                }
//                                                if (item == repository.getWordFromPref().dictName)
//                                                {
//
//                                                }
                                            },{ obj: Throwable -> obj.printStackTrace() })
                                }
                            }.setNegativeButton(R.string.button_text_no, null).create().show()
                }
                .setNegativeButton(R.string.button_text_cancel, null).create()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        subscribe?.dispose()
    }
}