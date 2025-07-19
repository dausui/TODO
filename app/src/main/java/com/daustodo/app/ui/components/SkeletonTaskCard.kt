package com.daustodo.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonTaskCard(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.4f),
        Color.LightGray.copy(alpha = 0.1f),
        Color.LightGray.copy(alpha = 0.4f)
    )
    
    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title skeleton (longer)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description skeleton (shorter)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom row with priority, date, and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority skeleton
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(brush)
                )
                
                // Date skeleton
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Pomodoro button skeleton
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(brush)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Complete button skeleton
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun SkeletonTaskList(
    itemCount: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(itemCount) {
            SkeletonTaskCard()
        }
    }
}

@Composable
fun SkeletonLoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Loading indicator with better visibility
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Loading text with better visibility
        Text(
            text = "Memuat tugas Anda...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Mohon tunggu sebentar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Skeleton cards with better spacing
        SkeletonTaskList(
            itemCount = 3,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun SkeletonLoadingStateWithProgress(
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Progress indicator
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 6.dp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Memuat data...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${(progress * 100).toInt()}% selesai",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Skeleton cards
        SkeletonTaskList(
            itemCount = 2,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}