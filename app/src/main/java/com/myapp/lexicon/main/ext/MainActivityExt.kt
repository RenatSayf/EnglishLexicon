package com.myapp.lexicon.main.ext

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.myapp.lexicon.databinding.DialogConfirmationBinding
import com.myapp.lexicon.dialogs.ConfirmDialog


fun AppCompatActivity.showWarningDialog(message: String) {
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