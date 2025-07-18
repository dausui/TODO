package com.daustodo.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.Task
import com.daustodo.app.ui.theme.*
import com.daustodo.app.utils.TimeUtils
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onCompleteClick: (Long) -> Unit,
    onDeleteClick: (Task) -> Unit,
    onPomodoroClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val cardColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "cardColor"
    )
    
    val contentAlpha = if (task.isCompleted) 0.6f else 1f
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClick(task) },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(contentAlpha)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Priority Indicator
                PriorityChip(
                    priority = task.priority,
                    modifier = Modifier.alpha(contentAlpha)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Middle Row - Category and Due Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.category.isNotBlank()) {
                        CategoryChip(
                            category = task.category,
                            modifier = Modifier.alpha(contentAlpha)
                        )
                    }
                    
                    if (task.pomodoroCount > 0) {
                        PomodoroCountChip(
                            count = task.pomodoroCount,
                            modifier = Modifier.alpha(contentAlpha)
                        )
                    }
                }
                
                if (task.dueDate != null) {
                    DueDateChip(
                        dueDate = task.dueDate,
                        modifier = Modifier.alpha(contentAlpha)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Complete Button
                    IconButton(
                        onClick = { onCompleteClick(task.id) }
                    ) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (task.isCompleted) "Mark as incomplete" else "Mark as complete",
                            tint = if (task.isCompleted) StatusCompleted else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Pomodoro Button
                    if (!task.isCompleted) {
                        IconButton(
                            onClick = { onPomodoroClick(task.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Start Pomodoro",
                                tint = PomodoroWork
                            )
                        }
                    }
                }
                
                // Delete Button
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Created time
            Text(
                text = "Created ${TimeUtils.formatRelativeTime(task.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(contentAlpha)
            )
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete '${task.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(task)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PriorityChip(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val color = when (priority) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW -> PriorityLow
    }
    
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = priority.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CategoryChip(
    category: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Label,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun PomodoroCountChip(
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = PomodoroWork.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = PomodoroWork,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                color = PomodoroWork
            )
        }
    }
}

@Composable
fun DueDateChip(
    dueDate: LocalDateTime,
    modifier: Modifier = Modifier
) {
    val now = LocalDateTime.now()
    val isOverdue = dueDate.isBefore(now)
    val isToday = TimeUtils.isToday(dueDate)
    
    val color = when {
        isOverdue -> StatusOverdue
        isToday -> StatusPending
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when {
                    isOverdue -> "Overdue"
                    isToday -> "Today"
                    else -> TimeUtils.formatDate(dueDate)
                },
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}