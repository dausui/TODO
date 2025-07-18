package com.daustodo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.Task
import com.daustodo.app.data.model.TaskFilter
import com.daustodo.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()
    
    private val _filter = MutableStateFlow(TaskFilter())
    val filter: StateFlow<TaskFilter> = _filter.asStateFlow()
    
    init {
        loadTasks()
        loadCategories()
    }
    
    private fun loadTasks() {
        viewModelScope.launch {
            combine(
                taskRepository.getFilteredTasks(_filter.value),
                _filter
            ) { tasks, filter ->
                _uiState.value = _uiState.value.copy(
                    tasks = tasks,
                    isLoading = false,
                    error = null
                )
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            taskRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }
    
    fun addTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM,
        category: String = "",
        dueDate: LocalDateTime? = null
    ) {
        viewModelScope.launch {
            try {
                val task = Task(
                    title = title,
                    description = description,
                    priority = priority,
                    category = category,
                    dueDate = dueDate
                )
                taskRepository.insertTask(task)
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.updateTask(task)
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(task)
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskCompletion(taskId)
                _uiState.value = _uiState.value.copy(error = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateFilter(filter: TaskFilter) {
        _filter.value = filter
        loadTasks()
    }
    
    fun searchTasks(query: String) {
        val newFilter = _filter.value.copy(searchQuery = query)
        _filter.value = newFilter
        loadTasks()
    }
    
    fun filterByCategory(category: String?) {
        val newFilter = _filter.value.copy(category = category)
        _filter.value = newFilter
        loadTasks()
    }
    
    fun filterByPriority(priority: Priority?) {
        val newFilter = _filter.value.copy(priority = priority)
        _filter.value = newFilter
        loadTasks()
    }
    
    fun showCompleted(show: Boolean) {
        val newFilter = _filter.value.copy(showCompleted = show)
        _filter.value = newFilter
        loadTasks()
    }
    
    fun showActive(show: Boolean) {
        val newFilter = _filter.value.copy(showActive = show)
        _filter.value = newFilter
        loadTasks()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    suspend fun getTaskById(id: Long): Task? {
        return taskRepository.getTaskById(id)
    }
}

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)