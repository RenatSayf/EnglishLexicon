package com.myapp.lexicon.schedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.myapp.lexicon.R
import com.myapp.lexicon.service.ServiceDialog

class AppNotification constructor(private val context: Context)
{
    companion object
    {
        const val NOTIFICATION_ID : Int = 15917
        const val CHANEL_ID : String = "${NOTIFICATION_ID}.service_notification"
    }

    fun show(title: String, content: String)
    {
        val actionIntent = Intent(Intent.ACTION_MAIN)
        actionIntent.setClass(context, ServiceDialog::class.java)
        actionIntent.flags = Intent.FLAG_ACTIVITY_MULTIPLE_TASK

        val pendingIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(context, CHANEL_ID)
                .setSmallIcon(R.drawable.ic_lexicon_notify)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val descriptionText = ""
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }


}