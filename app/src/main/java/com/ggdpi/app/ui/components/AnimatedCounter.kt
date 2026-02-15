package com.ggdpi.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedCounter(
    count: Long,
    label: String,
    modifier: Modifier = Modifier
) {
    var oldCount by remember { mutableStateOf(count) }
    
    SideEffect {
        oldCount = count
    }
    
    val countString = count.toString()
    val oldCountString = oldCount.toString()
    
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Row {
            countString.forEachIndexed { index, char ->
                val oldChar = oldCountString.getOrNull(index)
                val charValue = char.toString()
                
                AnimatedContent(
                    targetState = charValue,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInVertically { -it } + fadeIn() with
                            slideOutVertically { it } + fadeOut()
                        } else {
                            slideInVertically { it } + fadeIn() with
                            slideOutVertically { -it } + fadeOut()
                        }
                    },
                    label = "CounterAnimation"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}