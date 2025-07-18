package com.daustodo.app.ui.screens.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.Task
import com.daustodo.app.ui.theme.PriorityHigh
import com.daustodo.app.ui.theme.PriorityLow
import com.daustodo.app.ui.theme.PriorityMedium
import com.daustodo.app.viewmodel.TaskViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: Long? = null,
    onNavigateBack: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val taskUiState by taskViewModel.uiState.collectAsStateWithLifecycle()
    
    var task by remember { mutableStateOf<Task?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf<LocalDateTime?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPriorityDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    val isEditing = taskId != null
    val screenTitle = if (isEditing) "Edit Task" else "Add Task"
    
    // Load task for editing
    LaunchedEffect(taskId) {
        if (taskId != null) {
            task = taskViewModel.getTaskById(taskId)
            task?.let {
                title = it.title
                description = it.description
                category = it.category
                priority = it.priority
                dueDate = it.dueDate
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
                    text = screenTitle,
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
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            if (isEditing && task != null) {
                                taskViewModel.updateTask(
                                    task!!.copy(
                                        title = title,
                                        description = description,
                                        category = category,
                                        priority = priority,
                                        dueDate = dueDate
                                    )
                                )
                            } else {
                                taskViewModel.addTask(
                                    title = title,
                                    description = description,
                                    category = category,
                                    priority = priority,
                                    dueDate = dueDate
                                )
                            }
                            onNavigateBack()
                        }
                    }
                ) {
                    Text(if (isEditing) "Update" else "Save")
                }
            }
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                placeholder = { Text("Enter task title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null
                    )
                }
            )
            
            // Description Field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Enter task description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    )
                }
            )
            
            // Category Field
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category (Optional)") },
                placeholder = { Text("Enter category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (taskUiState.categories.isNotEmpty()) {
                        IconButton(
                            onClick = { showCategoryDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select category"
                            )
                        }
                    }
                }
            )
            
            // Priority Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showPriorityDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = when (priority) {
                                Priority.HIGH -> PriorityHigh
                                Priority.MEDIUM -> PriorityMedium
                                Priority.LOW -> PriorityLow
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Priority",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = priority.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
            
            // Due Date Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Due Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dueDate?.let { 
                                    "${it.dayOfMonth}/${it.monthValue}/${it.year}"
                                } ?: "No due date",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Row {
                        if (dueDate != null) {
                            IconButton(
                                onClick = { dueDate = null }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear due date"
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
            }
            
            // Delete Button (only for editing)
            if (isEditing && task != null) {
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedButton(
                    onClick = {
                        taskViewModel.deleteTask(task!!)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Task")
                }
            }
        }
    }
    
    // Priority Dialog
    if (showPriorityDialog) {
        AlertDialog(
            onDismissRequest = { showPriorityDialog = false },
            title = { Text("Select Priority") },
            text = {
                Column {
                    Priority.values().forEach { priorityOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = priority == priorityOption,
                                onClick = { priority = priorityOption }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = when (priorityOption) {
                                    Priority.HIGH -> PriorityHigh
                                    Priority.MEDIUM -> PriorityMedium
                                    Priority.LOW -> PriorityLow
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(priorityOption.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showPriorityDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Category Dialog
    if (showCategoryDialog && taskUiState.categories.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    taskUiState.categories.forEach { categoryOption ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = category == categoryOption,
                                onClick = { category = categoryOption }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(categoryOption)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showCategoryDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Date Picker (simplified - in real app you'd use a proper date picker)
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Set Due Date") },
            text = {
                Column {
                    Text("Select a due date for this task")
                    // In a real app, you'd use a proper DatePicker component
                    // For now, we'll set it to tomorrow as an example
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDate = LocalDateTime.now().plusDays(1)
                        showDatePicker = false
                    }
                ) {
                    Text("Tomorrow")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}