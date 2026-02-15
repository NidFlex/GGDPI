package com.ggdpi.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onAutoStartChanged: (Boolean) -> Unit,
    onDebugModeChanged: (Boolean) -> Unit,
    autoStart: Boolean = false,
    debugMode: Boolean = false
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text("Auto-start on boot") },
                supportingContent = { Text("Automatically start DPI bypass after device boot") },
                trailingContent = {
                    Switch(
                        checked = autoStart,
                        onCheckedChange = onAutoStartChanged
                    )
                }
            )
            
            Divider()
            
            ListItem(
                headlineContent = { Text("Debug mode") },
                supportingContent = { Text("Enable detailed logging") },
                trailingContent = {
                    Switch(
                        checked = debugMode,
                        onCheckedChange = onDebugModeChanged
                    )
                }
            )
            
            Divider()
            
            // Stats section
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            StatItem(title = "Total packets processed", value = "0")
            StatItem(title = "Active bypasses", value = "0")
            StatItem(title = "Blocked attempts", value = "0")
        }
    }
}

@Composable
fun StatItem(title: String, value: String) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = { Text(value, style = MaterialTheme.typography.titleMedium) }
    )
}