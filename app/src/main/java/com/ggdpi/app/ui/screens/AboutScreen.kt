package com.ggdpi.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GGDPI",
                style = MaterialTheme.typography.headlineLarge
            )
            
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Local DPI Bypass for Android",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "GGDPI is a local DPI (Deep Packet Inspection) bypass tool for Android. " +
                       "It works similarly to zapret-discord-youtube but runs entirely on your device " +
                       "without requiring external VPN servers.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Supported Services",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val services = listOf(
                "Instagram / Facebook",
                "YouTube / Google Video",
                "Discord (including voice calls)",
                "Telegram (MTProto)"
            )
            
            services.forEach { service ->
                ListItem(
                    headlineContent = { Text(service) },
                    leadingContent = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Open Source",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Based on techniques from zapret by bol-van and zapret-discord-youtube by Flowseal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { /* Open GitHub */ }) {
                Text("View on GitHub")
            }
        }
    }
}