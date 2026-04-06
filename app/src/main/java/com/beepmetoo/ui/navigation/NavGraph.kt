package com.beepmetoo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.beepmetoo.ui.detail.DetailScreen
import com.beepmetoo.ui.export.ExportScreen
import com.beepmetoo.ui.history.HistoryScreen
import com.beepmetoo.ui.home.HomeScreen
import com.beepmetoo.ui.record.RecordScreen
import com.beepmetoo.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val RECORD = "record?beepTimestamp={beepTimestamp}"
    const val HISTORY = "history"
    const val DETAIL = "detail/{sampleId}"
    const val SETTINGS = "settings"
    const val EXPORT = "export"

    fun record(beepTimestamp: Long? = null): String =
        if (beepTimestamp != null) "record?beepTimestamp=$beepTimestamp" else "record"

    fun detail(sampleId: Long): String = "detail/$sampleId"
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToRecord = { navController.navigate(Routes.record()) },
            )
        }

        composable(
            route = Routes.RECORD,
            arguments = listOf(
                navArgument("beepTimestamp") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) {
            RecordScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateToDetail = { sampleId ->
                    navController.navigate(Routes.detail(sampleId))
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("sampleId") { type = NavType.LongType },
            ),
        ) {
            DetailScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToExport = { navController.navigate(Routes.EXPORT) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.EXPORT) {
            ExportScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
