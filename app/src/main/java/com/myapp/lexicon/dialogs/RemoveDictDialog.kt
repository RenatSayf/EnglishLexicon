@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.DialogConfirmationBinding
import com.myapp.lexicon.databinding.TitleAlertDialogBinding
import com.myapp.lexicon.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


@AndroidEntryPoint
class RemoveDictDialog : DialogFragment()
{
    private lateinit var viewModel: MainViewModel
    private var subscribe: Disposable? = null

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

        val titleBinding = TitleAlertDialogBinding.inflate(layoutInflater, ConstraintLayout(requireContext()), false)
        with(titleBinding) {

            tvTitle.text = getString(R.string.title_del_dict)
            ivIconTitle.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash_can))
        }

        var confirmDialog: AlertDialog? = null
        return AlertDialog.Builder(requireContext(), R.style.AppAlertDialog)
            .setCustomTitle(titleBinding.root)
               .setMultiChoiceItems(inputArray, choice,
                   fun(dialog: DialogInterface, which: Int, isChecked: Boolean)
                   {
                       deleteItems.add(inputArray[which])
                   })
               .setPositiveButton(R.string.button_text_delete,
                   fun(dialog: DialogInterface, which: Int)
                   {
                       if (deleteItems.size <= 0) return

                       confirmDialog = AlertDialog.Builder(requireContext()).apply {
                           val binding = DialogConfirmationBinding.inflate(
                               layoutInflater,
                               ConstraintLayout(requireContext()),
                               false
                           )
                           with(binding) {
                               tvMessage.text = getString(R.string.dialog_are_you_sure)
                               btnOk.setOnClickListener {
                                   deleteItems.forEachIndexed { index, item ->
                                       subscribe = viewModel.deleteDict(item)
                                           .subscribeOn(Schedulers.newThread())
                                           .observeOn(AndroidSchedulers.mainThread())
                                           .subscribe({
                                               if (index == deleteItems.size - 1) {
                                                   Toast.makeText(
                                                       requireContext(),
                                                       R.string.msg_selected_dict_removed,
                                                       Toast.LENGTH_LONG
                                                   ).show()
                                                   dialogCallback.removeDictDialogButtonClickListener(
                                                       deleteItems
                                                   )
                                               }
                                               confirmDialog?.dismiss()
                                           }, { t: Throwable ->
                                               t.printStackTrace()
                                               confirmDialog?.dismiss()
                                           })
                                   }
                               }
                               btnCancel.setOnClickListener {
                                   confirmDialog?.dismiss()
                               }
                               setView(binding.root)
                           }
                       }.create().apply {
                           this.window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog)
                       }
                       confirmDialog?.show()
                   })
                .setNegativeButton(R.string.button_text_cancel, null)
            .create().apply {
                    this.window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog)
            }
    }

    override fun onDestroy()
    {
        super.onDestroy()
        subscribe?.dispose()
    }
}