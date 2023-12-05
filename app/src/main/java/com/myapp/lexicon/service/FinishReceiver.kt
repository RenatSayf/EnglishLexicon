package com.myapp.lexicon.service

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myapp.lexicon.main.MainActivity
import com.myapp.lexicon.main.MainFragment

class FinishReceiver(private val activity: MainActivity) : BroadcastReceiver() {

    private var triggerCount = 0
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            if (intent.action == Intent.ACTION_CLOSE_SYSTEM_DIALOGS) {

                val reason = intent.getStringExtra("reason")
                if (reason != null && reason == "homekey") {
                    triggerCount++
                    if (triggerCount == 1) {
                        val fragments = activity.supportFragmentManager.fragments
                        val mainFragment = fragments.firstOrNull {
                            it.javaClass.simpleName == MainFragment::class.java.simpleName
                        }
                        mainFragment?.let {
                            (it as MainFragment).finishVM.launchTimer()
                        }
                    }
                }
            }
        }
    }
}