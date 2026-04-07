package com.beepmetoo.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.beepmetoo.data.db.dao.ScheduledBeepDao
import com.beepmetoo.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BeepReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduledBeepDao: ScheduledBeepDao

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BeepScheduler.ACTION_BEEP) return

        val beepId = intent.getLongExtra(BeepScheduler.EXTRA_BEEP_ID, -1L)
        if (beepId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduledBeepDao.markFired(beepId)
                NotificationHelper.showBeepNotification(context, beepId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
