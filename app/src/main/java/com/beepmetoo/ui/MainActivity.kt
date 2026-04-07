package com.beepmetoo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.beepmetoo.ui.navigation.NavGraph
import com.beepmetoo.ui.navigation.Routes
import com.beepmetoo.ui.theme.BeepMeTooTheme
import com.beepmetoo.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val beepTimestamp = intent.getLongExtra(NotificationHelper.EXTRA_BEEP_TIMESTAMP, -1L)

        setContent {
            BeepMeTooTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startRoute = if (beepTimestamp > 0) Routes.record(beepTimestamp) else null,
                )
            }
        }
    }
}
