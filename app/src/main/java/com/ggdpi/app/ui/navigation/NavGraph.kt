package com.ggdpi.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ggdpi.app.ui.screens.*

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onRequestPermission = { /* TODO */ },
                onStartBypass = { /* TODO */ },
                onStopBypass = { /* TODO */ }
            )
        }
        
        composable(Screen.Strategy.route) {
            // Strategy selection screen
        }
        
        composable(Screen.HostList.route) {
            HostListScreen(
                hosts = emptyList(),
                onAddHost = {},
                onRemoveHost = {},
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAutoStartChanged = {},
                onDebugModeChanged = {}
            )
        }
        
        composable(Screen.Logs.route) {
            LogScreen(
                logs = emptyList(),
                onClearLogs = {},
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}