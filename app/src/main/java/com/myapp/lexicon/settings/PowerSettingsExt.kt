package com.myapp.lexicon.settings

import android.content.Context
import android.os.PowerManager

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

