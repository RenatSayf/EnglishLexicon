package com.myapp.lexicon.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.myapp.lexicon.R
import com.myapp.lexicon.databinding.TitleAlertDialogBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DictListDialog : DialogFragment()
{
    private var _selectedItem = MutableLiveData("")
    var selectedItem: LiveData<String> = _selectedItem

    companion object
    {
        val TAG = "${this::class.java.canonicalName}.TAG"
        var instance: DictListDialog? = null
        lateinit var listener: ISelectItemListener
        lateinit var list: MutableList<String>
        fun getInstance(list: MutableList<String>, listener: ISelectItemListener) : DictListDialog
        {
            this.list = list
            this.listener = listener
            return if (instance == null)
            {
                DictListDialog()
            }
            else instance!!
        }
    }

    interface ISelectItemListener
    {
        fun dictListItemOnSelected(dict: String)
    }

    @Suppress("ObjectLiteralToLambda")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {

        val titleBinding = TitleAlertDialogBinding.inflate(layoutInflater, ConstraintLayout(requireContext()), false)
        with(titleBinding) {

            tvTitle.text = getString(R.string.text_select_dict)
            ivIconTitle.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_translate))
        }

        val builder = AlertDialog.Builder(requireContext(), R.style.AppAlertDialog).apply {
            val stringArray = list.toTypedArray()
            setCustomTitle(titleBinding.root)
            setSingleChoiceItems(stringArray, 0, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, position: Int)
                {
                    listener.dictListItemOnSelected(list[position])
                    _selectedItem.value = list[position]
                    dismiss()
                }
            })
            setNegativeButton(R.string.text_cancel, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dismiss()
                }
            })
        }
        return builder.create().apply {
            this.window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog)
        }
    }
}