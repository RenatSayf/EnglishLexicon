package com.myapp.lexicon.main.ext

import android.view.View
import com.myapp.lexicon.databinding.DialogConfirmationBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.main.MainActivity


fun MainActivity.showWarningDialog(message: String) {
    ConfirmDialog.newInstance(onLaunch = {dialog: ConfirmDialog, binding: DialogConfirmationBinding ->
        with(binding) {
            tvEmoji.visibility = View.GONE
            tvEmoji2.visibility = View.GONE
            ivIcon.visibility = View.VISIBLE
            btnCancel.visibility = View.INVISIBLE
            tvMessage.text = message
            btnOk.setOnClickListener {
                dialog.dismiss()
            }
        }
    }).show(this.supportFragmentManager, ConfirmDialog.TAG)
}