package com.ggdpi.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ServiceToggle(
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ScaleAnimation"
    )
    
    val color by animateColorAsState(
        targetValue = if (isActive) Color(0xFF4CAF50) else Color(0xFFFF5722),
        animationSpec = tween(300),
        label = "ColorAnimation"
    )
    
    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale)
            .background(color, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.Power else Icons.Default.PowerOff,
            contentDescription = if (isActive) "Stop Service" else "Start Service",
            modifier = Modifier.size(56.dp),
            tint = Color.White
        )
    }
    
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(
            text = "Running",
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}