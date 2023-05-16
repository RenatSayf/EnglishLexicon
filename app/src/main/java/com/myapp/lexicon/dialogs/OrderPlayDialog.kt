package com.myapp.lexicon.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.myapp.lexicon.R

class OrderPlayDialog : DialogFragment()
{
    companion object
    {
        val TAG: String = "${this::class.java.canonicalName}.TAG"
        private var order = 0
        lateinit var listener: IDialogListener
        private var instance: OrderPlayDialog? = null

        fun getInstance(order: Int, listener: IDialogListener) : OrderPlayDialog
        {
            this.order = order
            this.listener = listener
            return if (instance == null)
            {
                OrderPlayDialog()
            }
            else instance as OrderPlayDialog
        }
    }
    interface IDialogListener
    {
        fun orderPlayDialogItemOnClick(order: Int)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        val builder = AlertDialog.Builder(requireContext()).apply {
            val stringArray = requireContext().resources.getStringArray(R.array.order_play_items)
            setSingleChoiceItems(stringArray, order, object : DialogInterface.OnClickListener
            {
                override fun onClick(p0: DialogInterface?, order: Int)
                {
                    listener.orderPlayDialogItemOnClick(order)
                    dismiss()
                }
            })
            setTitle(requireContext().getString(R.string.spinner_order_play_header))
        }
        return builder.create().apply {
            window?.setBackgroundDrawableResource(R.drawable.bg_popup_dialog_white)
        }
    }
}