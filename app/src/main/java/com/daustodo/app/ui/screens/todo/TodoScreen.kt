package com.daustodo.app.ui.screens.todo

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.Task
import com.daustodo.app.data.model.TaskFilter
import com.daustodo.app.ui.components.*
import com.daustodo.app.viewmodel.PomodoroViewModel
import com.daustodo.app.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    onNavigateToPomodoro: (Long?) -> Unit,
    onNavigateToPerformance: () -> Unit = {},
    taskViewModel: TaskViewModel = hiltViewModel(),
    pomodoroViewModel: PomodoroViewModel = hiltViewModel()
) {
    val taskUiState by taskViewModel.uiState.collectAsStateWithLifecycle()
    val taskFilter by taskViewModel.filter.collectAsStateWithLifecycle()
    val paginationState by taskViewModel.paginationState.collectAsStateWithLifecycle()
    val pomodoroUiState by pomodoroViewModel.uiState.collectAsStateWithLifecycle()
    
    var showFilterDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Update search when query changes
    LaunchedEffect(searchQuery) {
        taskViewModel.searchTasks(searchQuery)
    }
    
    // Handle error messages
    LaunchedEffect(taskUiState.error) {
        taskUiState.error?.let { error ->
            errorMessage = error
            showErrorSnackbar = true
            taskViewModel.clearError()
        }
    }
    
    // Handle pagination errors
    LaunchedEffect(paginationState.error) {
        paginationState.error?.let { error ->
            errorMessage = error
            showErrorSnackbar = true
            taskViewModel.clearPaginationError()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Daus Todo",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // Pomodoro Mini Timer
                if (pomodoroUiState.pomodoroState.isRunning || pomodoroUiState.pomodoroState.isPaused) {
                    PomodoroMiniTimer(
                        pomodoroState = pomodoroUiState.pomodoroState,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                // Search Button
                IconButton(
                    onClick = { showSearchBar = !showSearchBar }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
                
                // Performance Monitor Button
                IconButton(
                    onClick = onNavigateToPerformance
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Performance Monitor"
                    )
                }
                
                // Filter Button
                IconButton(
                    onClick = { showFilterDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter"
                    )
                }
            }
        )
        
        // Search Bar
        AnimatedVisibility(
            visible = showSearchBar,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari tugas...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp)
                )
            }
        }
        
        // Filter Chips
        if (taskFilter.category != null || taskFilter.priority != null || !taskFilter.showCompleted || !taskFilter.showActive) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (taskFilter.category != null) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { taskViewModel.filterByCategory(null) },
                            label = { Text("Kategori: ${taskFilter.category}") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                
                if (taskFilter.priority != null) {
                    item {
                        val priority = taskFilter.priority
                        FilterChip(
                            selected = true,
                            onClick = { taskViewModel.filterByPriority(null) },
                            label = { Text("Prioritas: ${priority?.displayName ?: "None"}") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                
                if (!taskFilter.showCompleted) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { taskViewModel.showCompleted(true) },
                            label = { Text("Sembunyikan Selesai") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
                
                if (!taskFilter.showActive) {
                    item {
                        FilterChip(
                            selected = true,
                            onClick = { taskViewModel.showActive(true) },
                            label = { Text("Sembunyikan Aktif") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove filter",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            PullToRefresh(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    taskViewModel.refreshTasks()
                    // Simulate refresh delay
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        kotlinx.coroutines.delay(1000)
                        isRefreshing = false
                    }
                }
            ) {
                when {
                    taskUiState.isLoading && taskUiState.tasks.isEmpty() -> {
                        // Show improved skeleton loading for initial load
                        SkeletonLoadingState(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                    
                    taskUiState.tasks.isEmpty() && !taskUiState.isLoading -> {
                        EmptyTasksState(
                            onAddTaskClick = onNavigateToAddTask,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    else -> {
                        LazyLoadingList(
                            items = taskUiState.tasks,
                            isLoading = paginationState.isLoadingMore,
                            hasMoreItems = paginationState.hasMoreItems,
                            onLoadMore = { taskViewModel.loadMoreTasks() },
                            modifier = Modifier.fillMaxSize(),
                            itemContent = { task ->
                                TaskCard(
                                    task = task,
                                    onTaskClick = { onNavigateToEditTask(task.id) },
                                    onCompleteClick = { taskViewModel.toggleTaskCompletion(task.id) },
                                    onDeleteClick = { taskViewModel.deleteTask(task) },
                                    onPomodoroClick = { onNavigateToPomodoro(task.id) },
                                    modifier = Modifier
                                )
                            }
                        )
                    }
                }
            }
            
            // Floating Action Button
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            currentFilter = taskFilter,
            categories = taskUiState.categories,
            onFilterChange = { taskViewModel.updateFilter(it) },
            onDismiss = { showFilterDialog = false }
        )
    }
    
    // Error Snackbar
    if (showErrorSnackbar) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(
                    onClick = { showErrorSnackbar = false }
                ) {
                    Text("Tutup")
                }
            },
            onDismiss = { showErrorSnackbar = false }
        ) {
            Text(errorMessage)
        }
    }
}

@Composable
fun EmptyTasksState(
    onAddTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Belum ada tugas",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tambahkan tugas pertama Anda untuk memulai produktivitas!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddTaskClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tambah Tugas")
        }
    }
}

@Composable
fun FilterDialog(
    currentFilter: TaskFilter,
    categories: List<String>,
    onFilterChange: (TaskFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Tasks") },
        text = {
            Column {
                // Show/Hide Options
                Text(
                    text = "Show",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tempFilter.showActive,
                        onCheckedChange = { tempFilter = tempFilter.copy(showActive = it) }
                    )
                    Text("Active tasks")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = tempFilter.showCompleted,
                        onCheckedChange = { tempFilter = tempFilter.copy(showCompleted = it) }
                    )
                    Text("Completed tasks")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority Filter
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = tempFilter.priority == null,
                        onClick = { tempFilter = tempFilter.copy(priority = null) },
                        label = { Text("All") }
                    )
                    
                    Priority.values().forEach { priority ->
                        FilterChip(
                            selected = tempFilter.priority == priority,
                            onClick = { tempFilter = tempFilter.copy(priority = priority) },
                            label = { Text(priority.displayName) }
                        )
                    }
                }
                
                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Category Filter
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = tempFilter.category == null,
                                onClick = { tempFilter = tempFilter.copy(category = null) },
                                label = { Text("All") }
                            )
                        }
                        
                        items(categories) { category ->
                            FilterChip(
                                selected = tempFilter.category == category,
                                onClick = { tempFilter = tempFilter.copy(category = category) },
                                label = { Text(category) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onFilterChange(tempFilter)
                    onDismiss()
                }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}