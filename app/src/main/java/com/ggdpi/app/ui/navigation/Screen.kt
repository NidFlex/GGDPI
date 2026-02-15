package com.ggdpi.app.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Strategy : Screen("strategy")
    object HostList : Screen("host_list")
    object Settings : Screen("settings")
    object Logs : Screen("logs")
    object About : Screen("about")
}