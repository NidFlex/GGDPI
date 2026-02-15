            containerColor = if (isActive) GreenActive else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) Color.White.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Power else Icons.Default.PowerOff,
                        contentDescription = "Toggle",
                        modifier = Modifier.size(48.dp),
                        tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isActive) "ACTIVE" else "INACTIVE",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                if (isActive) {
                    Text(
                        text = "DPI Bypass Running",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun StrategySelector(
    currentStrategy: StrategyManager.StrategyId,
    onClick: () -> Unit
) {
    val strategy = StrategyManager.strategies[currentStrategy]!!
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = strategy.displayName,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = strategy.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Select")
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "How it works",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "• Intercepts traffic using local VPN interface\n" +
                       "• Modifies packets to bypass DPI detection\n" +
                       "• Sends directly to internet (no external server)\n" +
                       "• Works with Instagram, YouTube, Discord, Telegram",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyBottomSheet(
    currentStrategy: StrategyManager.StrategyId,
    onStrategySelected: (StrategyManager.StrategyId) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Strategy",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyColumn {
                items(StrategyManager.strategies.toList()) { (id, strategy) ->
                    StrategyListItem(
                        strategy = strategy,
                        isSelected = id == currentStrategy,
                        onClick = { onStrategySelected(id) }
                    )
                }
            }
        }
    }
}

@Composable
fun StrategyListItem(
    strategy: StrategyManager.DpiStrategy,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(strategy.displayName) },
        supportingContent = { Text(strategy.description) },
        trailingContent = {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected")
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}