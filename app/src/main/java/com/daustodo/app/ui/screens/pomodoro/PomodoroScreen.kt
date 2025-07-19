package com.daustodo.app.ui.screens.pomodoro

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daustodo.app.data.model.Task
import com.daustodo.app.ui.components.PomodoroTimer
import com.daustodo.app.viewmodel.PomodoroViewModel
import com.daustodo.app.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    taskId: Long? = null,
    onNavigateBack: () -> Unit,
    pomodoroViewModel: PomodoroViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val pomodoroUiState by pomodoroViewModel.uiState.collectAsStateWithLifecycle()
    val taskUiState by taskViewModel.uiState.collectAsStateWithLifecycle()
    
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showTaskSelector by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var isLoadingTask by remember { mutableStateOf(false) }
    
    // Load selected task if taskId is provided
    LaunchedEffect(taskId) {
        if (taskId != null) {
            isLoadingTask = true
            try {
                selectedTask = taskViewModel.getTaskById(taskId)
                if (selectedTask != null) {
                    pomodoroViewModel.startSession(taskId)
                }
            } catch (e: Exception) {
                // Handle error silently or show error message
                selectedTask = null
            } finally {
                isLoadingTask = false
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Pomodoro Timer",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                // Settings Button
                IconButton(
                    onClick = { showSettings = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
                
                // Task Selector Button
                IconButton(
                    onClick = { showTaskSelector = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = "Select Task"
                    )
                }
            }
        )
        
        // Content
        if (pomodoroUiState.isLoading) {
            // Show loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Memuat Pomodoro...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Selected Task Card
                selectedTask?.let { task ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Sedang mengerjakan:",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        if (task.description.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = task.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = { 
                                            selectedTask = null
                                            pomodoroViewModel.stopSession()
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove task",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                
                                if (task.pomodoroCount > 0) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${task.pomodoroCount} sesi selesai",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // No Task Selected State
                if (selectedTask == null && !isLoadingTask) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(
                                    text = "Belum ada tugas dipilih",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Pilih tugas untuk memulai sesi Pomodoro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Button(
                                    onClick = { showTaskSelector = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pilih Tugas")
                                }
                            }
                        }
                    }
                }
                
                // Pomodoro Timer
                item {
                    PomodoroTimer(
                        pomodoroState = pomodoroUiState.pomodoroState,
                        onStartClick = { 
                            if (selectedTask != null) {
                                pomodoroViewModel.startTimer()
                            } else {
                                showTaskSelector = true
                            }
                        },
                        onPauseClick = { pomodoroViewModel.pauseTimer() },
                        onStopClick = { pomodoroViewModel.stopTimer() },
                        onSkipClick = { pomodoroViewModel.skipSession() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
                
                // Statistics Cards
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Today's Progress
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${pomodoroUiState.completedSessionsToday}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Hari Ini",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Total Sessions
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${pomodoroUiState.totalSessionsCompleted}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Quick Actions
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Aksi Cepat",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { pomodoroViewModel.resetSession() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset")
                                }
                                
                                OutlinedButton(
                                    onClick = { showTaskSelector = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pilih Tugas")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Task Selector Dialog
    if (showTaskSelector) {
        TaskSelectorDialog(
            tasks = taskUiState.tasks.filter { !it.isCompleted },
            onTaskSelected = { task ->
                selectedTask = task
                pomodoroViewModel.startSession(task.id)
                showTaskSelector = false
            },
            onDismiss = { showTaskSelector = false }
        )
    }
    
    // Settings Dialog
    if (showSettings) {
        PomodoroSettingsDialog(
            currentSettings = pomodoroUiState.settings,
            onSettingsChanged = { settings ->
                pomodoroViewModel.updateSettings(settings)
            },
            onDismiss = { showSettings = false }
        )
    }
    
    // Error handling
    pomodoroUiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar or handle error
            pomodoroViewModel.clearError()
        }
    }
}

@Composable
fun TaskSelectorDialog(
    tasks: List<Task>,
    onTaskSelected: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Tugas") },
        text = {
            if (tasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tidak ada tugas aktif tersedia",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        TaskSelectorItem(
                            task = task,
                            onClick = { onTaskSelected(task) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun TaskSelectorItem(
    task: Task,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun PomodoroSettingsDialog(
    currentSettings: PomodoroSettings,
    onSettingsChanged: (PomodoroSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSettings by remember { mutableStateOf(currentSettings) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pengaturan Pomodoro") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Work duration
                Column {
                    Text(
                        text = "Durasi Kerja (menit)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Slider(
                        value = tempSettings.workDuration.toFloat(),
                        onValueChange = { tempSettings = tempSettings.copy(workDuration = it.toInt()) },
                        valueRange = 1f..60f,
                        steps = 58
                    )
                    Text(
                        text = "${tempSettings.workDuration} menit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Short break duration
                Column {
                    Text(
                        text = "Istirahat Pendek (menit)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Slider(
                        value = tempSettings.shortBreakDuration.toFloat(),
                        onValueChange = { tempSettings = tempSettings.copy(shortBreakDuration = it.toInt()) },
                        valueRange = 1f..30f,
                        steps = 28
                    )
                    Text(
                        text = "${tempSettings.shortBreakDuration} menit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Long break duration
                Column {
                    Text(
                        text = "Istirahat Panjang (menit)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Slider(
                        value = tempSettings.longBreakDuration.toFloat(),
                        onValueChange = { tempSettings = tempSettings.copy(longBreakDuration = it.toInt()) },
                        valueRange = 5f..60f,
                        steps = 54
                    )
                    Text(
                        text = "${tempSettings.longBreakDuration} menit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Sessions before long break
                Column {
                    Text(
                        text = "Sesi sebelum Istirahat Panjang",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Slider(
                        value = tempSettings.sessionsBeforeLongBreak.toFloat(),
                        onValueChange = { tempSettings = tempSettings.copy(sessionsBeforeLongBreak = it.toInt()) },
                        valueRange = 2f..8f,
                        steps = 5
                    )
                    Text(
                        text = "${tempSettings.sessionsBeforeLongBreak} sesi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSettingsChanged(tempSettings)
                    onDismiss()
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

data class PomodoroSettings(
    val workDuration: Int = 25,
    val shortBreakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val sessionsBeforeLongBreak: Int = 4
)