package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myapp.lexicon.helpers.setServiceBroadcasts
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.MainFragment

class FinishReceiver : BroadcastReceiver() {

    private var activity: MainActivity? = null

    private var triggerCount = 0
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {

                val reason = intent.getStringExtra("reason")
                if (reason != null) {
                    triggerCount++
                    when (reason) {
                        "homekey" -> {
                            if (triggerCount == 1) {
                                val fragments = activity?.supportFragmentManager?.fragments
                                val mainFragment = fragments?.firstOrNull {
                                    it.javaClass.simpleName == MainFragment::class.java.simpleName
                                }
                                mainFragment?.let {
                                    (it as MainFragment).finishVM.launchTimer()
                                }
                            }
                        }
                        "recentapps" -> {
                            if (triggerCount == 1) {
                                context.setServiceBroadcasts()
                            }
                        }
                    }
                }
            }
        }
    }

    fun sendReferenceToActivity(activity: MainActivity) {
        this.activity = activity
    }
}