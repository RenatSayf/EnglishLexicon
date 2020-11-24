package com.myapp.lexicon.schedule

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat.getSystemService
import com.myapp.lexicon.R
import com.myapp.lexicon.helpers.AppOnStackTop
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.service.ServiceDialog

@Suppress("DEPRECATION")
class TimerReceiver : BroadcastReceiver()
{
    override fun onReceive(context: Context?, intent: Intent?)
    {
        if (context != null && intent != null)
        {
            when(intent.action)
            {
                AlarmScheduler.REPEAT_SHOOT_ACTION ->
                {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val displayVariant = preferences.getString(context.getString(R.string.key_on_unbloking_screen), "0")
                    println("********************* displayVariant = $displayVariant *********************")
                    when (displayVariant!!.toInt())
                    {
                        0 ->
                        {
                            val intentAct = Intent(context, ServiceDialog::class.java).apply {
                                action = Intent.ACTION_MAIN
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                            context.startActivity(intentAct)

                        }
                    }
                }
            }
        }
    }

}