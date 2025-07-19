package com.daustodo.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var pullOffset by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    
    val refreshThreshold = 100f
    val maxPullOffset = 200f
    
    val rotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val scale by animateFloatAsState(
        targetValue = if (pullOffset > 0) 1f else 0f,
        animationSpec = tween(200)
    )
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (pullOffset > refreshThreshold && !isRefreshing) {
                            scope.launch {
                                onRefresh()
                                delay(100)
                                pullOffset = 0f
                            }
                        } else {
                            pullOffset = 0f
                        }
                    }
                ) { _, dragAmount ->
                    if (!isRefreshing) {
                        pullOffset = (pullOffset + dragAmount.y).coerceIn(0f, maxPullOffset)
                    }
                }
            }
    ) {
        content()
        
        // Pull to refresh indicator
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 16.dp)
                .graphicsLayer {
                    alpha = scale
                    translationY = pullOffset * 0.5f
                }
        ) {
            Card(
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation)
                    )
                    
                    Text(
                        text = if (isRefreshing) "Refreshing..." else "Pull to refresh",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}