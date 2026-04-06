package com.beepmetoo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.beepmetoo.util.NotificationHelper

class BeepReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showBeepNotification(context)
    }
}
