package com.myapp.lexicon.schedule

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Entity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.myapp.lexicon.R
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.service.ServiceDialog

class AppNotification constructor(private val context: Context) : Notification()
{
    companion object
    {
        const val NOTIFICATION_ID : Int = 258974
        const val CHANEL_ID : String = "${NOTIFICATION_ID}.service_notification"
    }

    private lateinit var notification: Notification

    fun create(entity: DataBaseEntry) : Notification
    {
        val actionIntent = Intent(Intent.ACTION_MAIN)
        actionIntent.setClass(context, ServiceDialog::class.java).apply {
            putExtra("en", entity.english)
            putExtra("ru", entity.translate)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        //actionIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification = Notification()
        notification = NotificationCompat.Builder(context, CHANEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_lexicon_notify)
                .setContentTitle(entity.english)
                .setContentText(entity.translate)
                .setStyle(NotificationCompat.BigTextStyle().bigText(entity.translate))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setChannelId(CHANEL_ID)
                .setDefaults(DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

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