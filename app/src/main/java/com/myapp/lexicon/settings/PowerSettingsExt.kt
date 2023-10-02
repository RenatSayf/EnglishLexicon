package com.myapp.lexicon.settings

import android.content.Context
import android.os.PowerManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.myapp.lexicon.dialogs.ConfirmDialog

const val KEY_BATTERY_SETTINGS = "KEY_BATTERY_SETTINGS"
const val BATTERY_SETTINGS = 35654458

fun Context.isIgnoringBatteryOptimizations(
    onOptimizingUse: () -> Unit = {},
    onNotUse: () -> Unit = {}
) {
    val powerManager = this.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    val packageName = this.applicationContext.packageName
    if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
        onNotUse.invoke()
    }
    else {
        onOptimizingUse.invoke()
    }
}

fun AppCompatActivity.checkBatterySettings(
    onGoToSettings: () -> Unit,
    onFinish: () -> Unit
) {
    isIgnoringBatteryOptimizations(
        onNotUse = {
            onFinish.invoke()
        },
        onOptimizingUse = {
            ConfirmDialog.newInstance(
                onLaunch = {dialog, binding ->
                    with(binding) {
                        val message = "Для стабильной работы приложения в режиме Пассивного изучения, необходимо отключить оптимизацию работы батареи"
                        tvMessage.text = message
                        btnOk.apply {
                            text = "К настройкам"
                            setOnClickListener {
                                dialog.dismiss()
                                onGoToSettings.invoke()
                            }
                        }
                        btnCancel.apply {
                            visibility = View.VISIBLE
                            setOnClickListener {
                                dialog.dismiss()
                                onFinish.invoke()
                            }
                        }
                    }
                }
            ).show(this.supportFragmentManager, ConfirmDialog.TAG)
        }
    )
}