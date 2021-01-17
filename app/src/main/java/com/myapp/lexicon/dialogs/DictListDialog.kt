package com.myapp.lexicon.dialogs

import androidx.fragment.app.DialogFragment

class DictListDialog : DialogFragment()
{
    companion object
    {
        val TAG = "${this::class.java.canonicalName}.TAG"
        var instance: DictListDialog? = null
        lateinit var listener: ISelectItemListener
        fun getInstance(listener: ISelectItemListener) : DictListDialog
        {
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
}