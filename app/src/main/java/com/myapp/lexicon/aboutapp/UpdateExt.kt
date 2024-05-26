@file:Suppress("ObjectLiteralToLambda")

package com.myapp.lexicon.aboutapp

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.myapp.lexicon.R
import com.myapp.lexicon.dialogs.ConfirmDialog
import java.util.concurrent.TimeUnit


fun Context.checkAppUpdate(
    onAvailable: (info: AppUpdateInfo?) -> Unit,
    onNotAvailable: () -> Unit = {}
) {
    val updateManager = AppUpdateManagerFactory.create(this)
    updateManager.appUpdateInfo.addOnSuccessListener(object : OnSuccessListener<AppUpdateInfo> {
        override fun onSuccess(info: AppUpdateInfo?) {
            if (info?.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                onAvailable.invoke(info)
            }
            else {
                onNotAvailable.invoke()
            }
        }
    })
}

fun View.showUpdateSnackBar(
    onClick: () -> Unit
) {
    val snackBar = Snackbar.make(
        this,
        this.context.getString(R.string.text_update_available),
        TimeUnit.SECONDS.toMillis(30).toInt()
    )
    snackBar.apply {
        setAction(R.string.text_update, object : View.OnClickListener {
            override fun onClick(p0: View?) {
                onClick.invoke()
            }
        })
        setActionTextColor(ContextCompat.getColor(this.context, R.color.colorLightGreen))
    }
    snackBar.show()
}

fun FragmentActivity.showUpdateDialog(
    onClick: () -> Unit
) {
    ConfirmDialog.newInstance { dialog, binding ->
        with(binding) {
            tvEmoji.visibility = View.GONE
            tvEmoji2.visibility = View.GONE
            ivIcon.visibility = View.VISIBLE
            tvMessage.text = this@showUpdateDialog.getString(R.string.text_important_update_availaible)
            btnCancel.visibility = View.GONE
            btnOk.apply {
                text = this@showUpdateDialog.getString(R.string.text_update)
                setOnClickListener {
                    onClick.invoke()
                    dialog.dismiss()
                }
            }
        }
    }.show(this.supportFragmentManager, ConfirmDialog.TAG)
}