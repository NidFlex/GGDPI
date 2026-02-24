package com.ggdpi.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ggdpi.app.data.HostListRepository
import com.ggdpi.app.ui.screens.*
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route,
    onRequestPermission: () -> Unit,
    onStartBypass: () -> Unit,
    onStopBypass: () -> Unit
) {
    val context = LocalContext.current
    val hostRepo = remember { HostListRepository(context) }
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onRequestPermission = onRequestPermission,
                onStartBypass = onStartBypass,
                onStopBypass = onStopBypass,
                onOpenHostList = { navController.navigate(Screen.HostList.route) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
                onOpenLogs = { navController.navigate(Screen.Logs.route) },
                onOpenAbout = { navController.navigate(Screen.About.route) }
            )
        }
        
        composable(Screen.Strategy.route) {
            // Strategy selection screen
        }
        
        composable(Screen.HostList.route) {
            val hosts by hostRepo.customHosts.collectAsState(initial = emptySet())
            val updateUrl by hostRepo.customHostsUpdateUrl.collectAsState(initial = "")
            val lastUpdated by hostRepo.customHostsLastUpdatedEpochMs.collectAsState(initial = 0L)
            var isUpdating by remember { mutableStateOf(false) }
            var updateError by remember { mutableStateOf<String?>(null) }

            HostListScreen(
                hosts = hosts.toList().sorted(),
                updateUrl = updateUrl,
                lastUpdatedEpochMs = lastUpdated,
                isUpdating = isUpdating,
                updateError = updateError,
                onUpdate = {
                    if (updateUrl.isBlank() || isUpdating) return@HostListScreen
                    scope.launch {
                        isUpdating = true
                        updateError = null
                        try {
                            hostRepo.updateCustomHostsFromUrl(updateUrl)
                        } catch (e: Exception) {
                            updateError = e.message ?: "Update failed"
                        } finally {
                            isUpdating = false
                        }
                    }
                },
                onAddHost = { host ->
                    scope.launch { hostRepo.addCustomHost(host) }
                },
                onRemoveHost = { host ->
                    scope.launch { hostRepo.removeCustomHost(host) }
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            val updateUrl by hostRepo.customHostsUpdateUrl.collectAsState(initial = "")
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onDebugModeChanged = {},
                onCustomHostsUpdateUrlChanged = { url ->
                    scope.launch { hostRepo.setCustomHostsUpdateUrl(url) }
                },
                customHostsUpdateUrl = updateUrl
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