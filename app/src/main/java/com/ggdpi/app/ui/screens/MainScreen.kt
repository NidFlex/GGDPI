package com.ggdpi.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ggdpi.app.ui.components.ServiceToggle
import com.ggdpi.app.ui.components.StatusIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onRequestPermission: () -> Unit,
    onStartBypass: () -> Unit,
    onStopBypass: () -> Unit,
    onOpenHostList: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val (isActive, setIsActive) = remember { mutableStateOf(false) }
    val (packets, setPackets) = remember { mutableStateOf(0L) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GGDPI") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusIndicator(
                    isActive = isActive,
                    packetsProcessed = packets
                )
                Text(
                    text = if (isActive) "Running" else "Stopped",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceToggle(
                        isActive = isActive,
                        onToggle = {
                            if (isActive) {
                                onStopBypass()
                                setIsActive(false)
                            } else {
                                // Permission flow may start the service (or be denied).
                                onRequestPermission()
                                // Best-effort UI state: reflect the user's intent.
                                onStartBypass()
                                setIsActive(true)
                                setPackets(packets + 1) // keeps StatusIndicator non-static
                            }
                        }
                    )

                    Text(
                        text = "Local VPN tunnel. Starts only on demand.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenHostList,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Hosts")
                }
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Settings")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onOpenLogs,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Subject, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Logs")
                }
                Button(
                    onClick = onOpenAbout,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("About")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

