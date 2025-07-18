package com.daustodo.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daustodo.app.data.model.PomodoroState
import com.daustodo.app.data.model.PomodoroType
import com.daustodo.app.ui.theme.*
import com.daustodo.app.utils.TimeUtils
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PomodoroTimer(
    pomodoroState: PomodoroState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTime = pomodoroState.currentType.defaultDuration * 60
    val progress = TimeUtils.getProgressPercentage(pomodoroState.timeRemaining, totalTime)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progress"
    )
    
    val timerColor = when (pomodoroState.currentType) {
        PomodoroType.WORK -> PomodoroWork
        PomodoroType.SHORT_BREAK -> PomodoroBreak
        PomodoroType.LONG_BREAK -> PomodoroLongBreak
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Session Type and Counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = when (pomodoroState.currentType) {
                    PomodoroType.WORK -> Icons.Default.Work
                    PomodoroType.SHORT_BREAK -> Icons.Default.Coffee
                    PomodoroType.LONG_BREAK -> Icons.Default.RestaurantMenu
                },
                contentDescription = null,
                tint = timerColor
            )
            Text(
                text = pomodoroState.currentType.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = timerColor
            )
            Text(
                text = "${pomodoroState.currentSession}/${pomodoroState.totalSessions}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Circular Timer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            // Progress Circle
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerX = size.width / 2
                val centerY = size.height / 2
                
                // Background circle
                drawCircle(
                    color = timerColor.copy(alpha = 0.1f),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
                
                // Progress arc
                if (animatedProgress > 0) {
                    drawArc(
                        color = timerColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            
            // Timer Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = TimeUtils.formatTime(pomodoroState.timeRemaining),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 48.sp,
                    color = timerColor
                )
                
                Text(
                    text = when {
                        pomodoroState.isRunning -> "Running"
                        pomodoroState.isPaused -> "Paused"
                        else -> "Ready"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stop Button
            if (pomodoroState.isRunning || pomodoroState.isPaused) {
                IconButton(
                    onClick = onStopClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Main Play/Pause Button
            FloatingActionButton(
                onClick = {
                    if (pomodoroState.isRunning) {
                        onPauseClick()
                    } else {
                        onStartClick()
                    }
                },
                containerColor = timerColor,
                contentColor = Color.White,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (pomodoroState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (pomodoroState.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Skip Button
            if (pomodoroState.isRunning || pomodoroState.isPaused) {
                IconButton(
                    onClick = onSkipClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Skip",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Session Progress Dots
        SessionProgressDots(
            currentSession = pomodoroState.currentSession,
            totalSessions = pomodoroState.totalSessions,
            activeColor = timerColor
        )
    }
}

@Composable
fun SessionProgressDots(
    currentSession: Int,
    totalSessions: Int,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSessions) { index ->
            val isActive = index < currentSession
            val dotColor = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .then(
                        if (isActive) {
                            Modifier.background(dotColor)
                        } else {
                            Modifier.background(dotColor)
                        }
                    )
            )
        }
    }
}

@Composable
fun PomodoroMiniTimer(
    pomodoroState: PomodoroState,
    modifier: Modifier = Modifier
) {
    val timerColor = when (pomodoroState.currentType) {
        PomodoroType.WORK -> PomodoroWork
        PomodoroType.SHORT_BREAK -> PomodoroBreak
        PomodoroType.LONG_BREAK -> PomodoroLongBreak
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = timerColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = timerColor,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = TimeUtils.formatTime(pomodoroState.timeRemaining),
                style = MaterialTheme.typography.labelMedium,
                color = timerColor,
                fontWeight = FontWeight.SemiBold
            )
            
            if (pomodoroState.isRunning) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Running",
                    tint = timerColor,
                    modifier = Modifier.size(12.dp)
                )
            } else if (pomodoroState.isPaused) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Paused",
                    tint = timerColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}