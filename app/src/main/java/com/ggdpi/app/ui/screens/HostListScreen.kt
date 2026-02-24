package com.ggdpi.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostListScreen(
    hosts: List<String>,
    updateUrl: String,
    lastUpdatedEpochMs: Long,
    isUpdating: Boolean,
    updateError: String?,
    onUpdate: () -> Unit,
    onAddHost: (String) -> Unit,
    onRemoveHost: (String) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newHost by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Custom Hosts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onUpdate,
                        enabled = !isUpdating && updateUrl.isNotBlank()
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Update List")
                        }
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Host")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Text(
                    text = "Domains for DPI bypass:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                val lastUpdated = remember(lastUpdatedEpochMs) {
                    if (lastUpdatedEpochMs <= 0L) {
                        "Never"
                    } else {
                        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            .format(Date(lastUpdatedEpochMs))
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Update URL: ${if (updateUrl.isBlank()) "Not set" else updateUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Last updated: $lastUpdated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    updateError?.takeIf { it.isNotBlank() }?.let { err ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            items(hosts) { host ->
                HostListItem(
                    host = host,
                    onDelete = { onRemoveHost(host) }
                )
            }
            
            if (hosts.isEmpty()) {
                item {
                    EmptyHostList()
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                newHost = ""
            },
            title = { Text("Add Host") },
            text = {
                OutlinedTextField(
                    value = newHost,
                    onValueChange = { newHost = it },
                    label = { Text("Domain (e.g., example.com)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newHost.isNotBlank()) {
                                onAddHost(newHost.trim())
                                newHost = ""
                                showAddDialog = false
                            }
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newHost.isNotBlank()) {
                            onAddHost(newHost.trim())
                            newHost = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    newHost = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HostListItem(host: String, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(host) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    )
}

@Composable
fun EmptyHostList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.List,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No custom hosts",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Add domains to apply DPI bypass",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}