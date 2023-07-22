package com.myapp.lexicon.schedule

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.myapp.lexicon.R
import com.myapp.lexicon.models.toWordList
import com.myapp.lexicon.service.ServiceActivity
import com.myapp.lexicon.splash.SplashActivity


class AppNotification constructor(private val context: Context) : Notification()
{
    companion object
    {
        const val NOTIFICATION_ID : Int = 258974
        const val CHANNEL_ID : String = "${NOTIFICATION_ID}.service_notification"
    }

    private lateinit var notification: Notification

    fun create(text: String) : Notification
    {
        val words = text.toWordList()

        notification = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setOngoing(false)
            setSmallIcon(R.drawable.ic_translate_blue_24dp)
            color = ContextCompat.getColor(context, R.color.colorAccent)
            setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.icon_logo))
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
            setChannelId(CHANNEL_ID)
            setDefaults(DEFAULT_ALL)

            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val displayMode = preferences.getString(context.getString(R.string.key_list_display_mode), "0").toString()
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
                    putExtra(ServiceActivity.ARG_JSON, text)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    actionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setContentIntent(pendingIntent)
            }
            else
            {
                setContentTitle(context.getString(R.string.text_all_words_learned))
                setContentText(context.getString(R.string.text_select_other_dict))
                val intent = Intent(context, SplashActivity::class.java)
                val activity = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setContentIntent(activity)
            }
        }.build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = context.getString(R.string.app_name)
        val descriptionText = ""
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)

        return notification
    }

    fun show()
    {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun isVisible(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val activeNotifications = notificationManager.activeNotifications
        val notification = try {
            activeNotifications.first {
                it.id == NOTIFICATION_ID
            }
        } catch (e: Exception) {
            null
        }
        return notification != null
    }

}