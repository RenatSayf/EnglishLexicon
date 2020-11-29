package com.myapp.lexicon.schedule

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.myapp.lexicon.R
import com.myapp.lexicon.database.DataBaseEntry
import com.myapp.lexicon.service.ModalFragment
import com.myapp.lexicon.service.ServiceActivity

class AppNotification constructor(private val context: Context) : Notification()
{
    companion object
    {
        const val NOTIFICATION_ID : Int = 258974
        const val CHANEL_ID : String = "${NOTIFICATION_ID}.service_notification"
    }

    init
    {

    }

    private lateinit var notification: Notification

    fun create(json: String) : Notification
    {
        val actionIntent = Intent(Intent.ACTION_MAIN)
        actionIntent.setClass(context, ServiceActivity::class.java).apply {
            putExtra(ModalFragment.ARG_JSON, json)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        var pair: Pair<Map<String, Int>, List<DataBaseEntry>>? = null
        val type = object : TypeToken<Pair<Map<String?, Int?>?, List<DataBaseEntry?>?>?>()
        {}.type

        try
        {
            val obj = Gson().fromJson<Any>(json, type)
            @Suppress("UNCHECKED_CAST")
            pair = obj as Pair<Map<String, Int>, List<DataBaseEntry>>
        }
        catch (e: JsonSyntaxException)
        {
            e.printStackTrace()
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        notification = Notification()
        notification = NotificationCompat.Builder(context, CHANEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_lexicon_notify)
                .setContentTitle(pair?.second?.get(0)?.english ?: "JSON ERROR!!!!!!!!!!!!!!!!!!")
                .setContentText(pair?.second?.get(0)?.translate ?: "JSON ERROR!!!!!!!!!!!!!!!!!!")
                .setStyle(NotificationCompat.BigTextStyle().bigText(pair?.second?.get(0)?.translate ?: "JSON ERROR!!!!!!!!!!!!!!!!!!"))
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