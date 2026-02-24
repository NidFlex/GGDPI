package com.ggdpi.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ggdpi.app.core.StrategyManager

@Composable
fun StrategyCard(
    strategy: StrategyManager.DpiStrategy,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strategy.displayName,
                    style = MaterialTheme.typography.titleMedium
                )

                if (isSelected) {
                    Badge {
                        Text("Active")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = strategy.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Technical details - заменено FlowRow на стабильный Row с переносом
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text(strategy.desyncMode) }
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text("×${strategy.repeats}") }
                    )
                }
                strategy.fakeSni?.let { sni ->
                    Row(
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text("fake:$sni") }
                        )
                    }
                }
            }
        }
    }
}