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
    onDebugModeChanged: (Boolean) -> Unit,
    onCustomHostsUpdateUrlChanged: (String) -> Unit,
    debugMode: Boolean = false,
    customHostsUpdateUrl: String = ""
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

            Text(
                text = "Lists",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            OutlinedTextField(
                value = customHostsUpdateUrl,
                onValueChange = onCustomHostsUpdateUrlChanged,
                label = { Text("Custom hosts update URL") },
                supportingText = { Text("Used when you press “Update” in Custom Hosts") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
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