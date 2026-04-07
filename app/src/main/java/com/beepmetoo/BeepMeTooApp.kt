package com.beepmetoo

import android.app.Application
import com.beepmetoo.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BeepMeTooApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
