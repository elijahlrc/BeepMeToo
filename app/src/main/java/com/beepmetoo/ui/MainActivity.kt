package com.beepmetoo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.beepmetoo.ui.navigation.NavGraph
import com.beepmetoo.ui.theme.BeepMeTooTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeepMeTooTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
