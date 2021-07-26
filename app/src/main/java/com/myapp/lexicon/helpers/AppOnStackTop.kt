package com.myapp.lexicon.helpers

import android.app.ActivityManager
import android.app.Service
import android.content.Context

object AppOnStackTop
{
    // TODO: ActivityManager.RunningAppProcessInfo Проверка, что активити находится на верху стека
    fun check(context: Context): Boolean
    {
        val activityManager = (context.getSystemService(Service.ACTIVITY_SERVICE)) as ActivityManager?
        var runningAppProcesses: List<ActivityManager.RunningAppProcessInfo>? = null
        if (activityManager != null)
        {
            runningAppProcesses = activityManager.runningAppProcesses
        }
        if (runningAppProcesses != null && runningAppProcesses.isNotEmpty())
        {
            val processName = runningAppProcesses[0].processName
            val packageName: String = context.applicationInfo.packageName
            println("******************** processName = $processName *****************************")
            println("******************** packageName = $packageName *****************************")
            return processName == packageName
        }
        return false
    }
}