@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package com.myapp.lexicon.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.TitleAlertDialogBinding
import com.myapp.lexicon.helpers.LockOrientation
import com.myapp.lexicon.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RemoveDictDialog : DialogFragment()
{
    private lateinit var viewModel: MainViewModel
    private val locker by lazy {
        LockOrientation(requireActivity())
    }

    companion object
    {
        const val TAG = "remove_dict_dialog"
        const val ARG_INPUT = "arg_input_list"
        private var instance: RemoveDictDialog? = null
        private lateinit var listener: Listener

        fun getInstance(list: ArrayList<String>, listener: Listener) : RemoveDictDialog = if (instance == null)
        {
            this.listener = listener
            RemoveDictDialog().apply {
                arguments = Bundle().apply {
                    val array : Array<out String> = list.toArray(emptyArray())
                    putStringArray(ARG_INPUT, array)
                }
            }
        }
        else instance!!
    }

    interface Listener
    {
        fun onRemoveButtonClick(list: MutableList<String>)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        locker.lock()
        val inputArray: Array<String> = arguments?.getStringArray(ARG_INPUT) as Array<String>
        val deleteItems = ArrayList<String>()
        val choice = BooleanArray(inputArray.size)

        val titleBinding = TitleAlertDialogBinding.inflate(layoutInflater, ConstraintLayout(requireContext()), false)
        with(titleBinding) {

            tvTitle.text = getString(R.string.title_del_dict)
            ivIconTitle.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_trash_can))
        }

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
                       listener.onRemoveButtonClick(deleteItems)
                   })
                .setNegativeButton(R.string.button_text_cancel, null)
            .create().apply {
                    this.window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog)
            }
    }

    override fun onDestroyView() {
        locker.unLock()
        super.onDestroyView()
    }

}