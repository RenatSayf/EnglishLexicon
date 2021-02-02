package com.myapp.lexicon.schedule

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.myapp.lexicon.R
import com.myapp.lexicon.database.Word
import com.myapp.lexicon.helpers.StringOperations
import com.myapp.lexicon.main.SplashScreenActivity
import com.myapp.lexicon.service.LexiconService
import com.myapp.lexicon.service.ServiceActivity

class AppNotification constructor(private val context: Context) : Notification()
{
    companion object
    {
        const val NOTIFICATION_ID : Int = 258974
        const val CHANEL_ID : String = "${NOTIFICATION_ID}.service_notification"
    }

    private var preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private lateinit var notification: Notification
    private var displayMode: String = "0"

    init
    {
        displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0").toString()
    }

    fun create(json: String) : Notification
    {
        val words: Array<Word> = StringOperations.instance.jsonToWord(json)

        notification = NotificationCompat.Builder(context, CHANEL_ID).apply {
            setOngoing(true)
            setSmallIcon(R.drawable.ic_lexicon_notify)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_MAX
            setChannelId(CHANEL_ID)
            setDefaults(DEFAULT_SOUND)

            if (words.isNotEmpty())
            {
                setContentTitle(words[0].english)
                if (displayMode == "1")
                {
                    setContentText("?????????????")
                }
                else setContentText(words[0].translate)

                val actionIntent = Intent(Intent.ACTION_MAIN)
                actionIntent.setClass(context, ServiceActivity::class.java).apply {
                    putExtra(LexiconService.ARG_JSON, json)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val pendingIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                setContentIntent(pendingIntent)
                //println("******************************* ${words[0].english}***********************")
            }
            else
            {
                setContentTitle(context.getString(R.string.text_all_words_learned))
                setContentText(context.getString(R.string.text_select_other_dict))
                val intent = Intent(context, SplashScreenActivity::class.java)
                val activity = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                setContentIntent(activity)
            }
        }.build()



        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = context.getString(R.string.app_name)
            val descriptionText = ""
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        return notification
    }

    fun show()
    {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }



}