package com.myapp.lexicon.main.ext

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.myapp.lexicon.R
import com.myapp.lexicon.ads.models.AdData
import com.myapp.lexicon.ads.models.AdData.Companion.fromString
import com.myapp.lexicon.databinding.DialogConfirmationBinding
import com.myapp.lexicon.dialogs.ConfirmDialog
import com.myapp.lexicon.models.to2DigitsScale
import com.myapp.lexicon.settings.userPercentFromPref
import com.myapp.lexicon.splash.SplashActivity


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

fun FragmentActivity.showThankDialog(
    message: String,
    onDismissed: () -> Unit = {}
) {
    ConfirmDialog.newInstance(onLaunch = {dialog: ConfirmDialog, binding: DialogConfirmationBinding ->
        with(binding) {
            ivIcon.visibility = View.INVISIBLE
            tvEmoji.apply {
                text = getString(R.string.slightly_smiling_face)
                visibility = View.VISIBLE
            }
            tvEmoji2.apply {
                text = getString(R.string.thumbs_up)
                visibility = View.VISIBLE
            }
            btnCancel.visibility = View.INVISIBLE
            tvMessage.text = message
            btnOk.setOnClickListener {
                onDismissed.invoke()
                dialog.dismiss()
            }
        }
    }).show(this.supportFragmentManager, ConfirmDialog.TAG)
}

fun FragmentActivity.handleAdDataFromSplashActivity(
    onCompleted: (AdData?, Double) -> Unit
) {
    if (this.intent != null) {
        val strAdData = this.intent.getStringExtra(SplashActivity.KEY_AD_DATA)
        if (strAdData != null) {
            val adData: AdData? = strAdData.fromString()
            if (adData != null) {
                val bonus = (adData.revenue * this.userPercentFromPref).to2DigitsScale()
                this.intent.putExtra(SplashActivity.KEY_AD_DATA, null as String?)
                onCompleted.invoke(adData, bonus)
            }
            else {
                onCompleted.invoke(null, 0.0)
            }
        }
        else {
            onCompleted.invoke(null, 0.0)
        }
    }
    else {
        onCompleted.invoke(null, 0.0)
    }
}