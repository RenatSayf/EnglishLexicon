package com.myapp.lexicon.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.myapp.lexicon.R
import com.myapp.lexicon.main.MainViewModel
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
            else instance as DictListDialog
        }
    }

    interface ISelectItemListener
    {
        fun dictListItemOnSelected(dict: String)
    }

    @Suppress("ObjectLiteralToLambda")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(requireContext()).apply {
            val stringArray = list.toTypedArray()
            setSingleChoiceItems(stringArray, 0, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, position: Int)
                {
                    listener.dictListItemOnSelected(list[position])
                    _selectedItem.value = list[position]
                    dismiss()
                }
            })
            setTitle(getString(R.string.text_dictionaries))
            setCancelable(false)
            isCancelable = false
        }
        return builder.create()
    }
}