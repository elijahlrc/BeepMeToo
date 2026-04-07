package com.beepmetoo.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.beepmetoo.R
import com.beepmetoo.ui.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "beep_channel"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Beep Notifications",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifications for experience sampling beeps"
            enableVibration(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun showBeepNotification(context: Context, beepId: Long) {
        createChannel(context)

        val beepTimestamp = System.currentTimeMillis()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_BEEP_TIMESTAMP, beepTimestamp)
            putExtra(EXTRA_BEEP_ID, beepId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            beepId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("BeepMeToo")
            .setContentText("Time to record your experience!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(beepId.toInt(), notification)
    }

    const val EXTRA_BEEP_TIMESTAMP = "beepTimestamp"
    const val EXTRA_BEEP_ID = "beepId"
}
