package com.daustodo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.Task
import com.daustodo.app.data.model.TaskFilter
import com.daustodo.app.data.repository.TaskRepository
import com.daustodo.app.utils.BackgroundTaskManager
import com.daustodo.app.utils.CacheManager
import com.daustodo.app.utils.PerformanceMonitor
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
    
    // Pagination support
    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()
    
    // Cache for tasks
    private val taskCache = CacheManager<List<Task>>(maxSize = 50, defaultExpiryMinutes = 5)
    private val categoryCache = CacheManager<List<String>>(maxSize = 10, defaultExpiryMinutes = 30)
    
    // Performance tracking
    private var lastLoadTime = 0L
    
    init {
        loadTasks()
        loadCategories()
        
        // Clean up expired cache entries periodically
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000) // Every minute
                taskCache.removeExpired()
                categoryCache.removeExpired()
            }
        }
    }
    
    private fun loadTasks(isRefresh: Boolean = false) {
        if (isRefresh) {
            taskCache.clear()
        }
        
        viewModelScope.launch {
            PerformanceMonitor.measureSuspendOperation("load_tasks") {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                try {
                    val cacheKey = "tasks_${_filter.value.hashCode()}"
                    val cachedTasks = taskCache.get(cacheKey)
                    
                    if (cachedTasks != null && !isRefresh) {
                        _uiState.value = _uiState.value.copy(
                            tasks = cachedTasks,
                            isLoading = false,
                            error = null
                        )
                        return@measureSuspendOperation
                    }
                    
                    combine(
                        taskRepository.getFilteredTasks(_filter.value),
                        _filter
                    ) { tasks, filter ->
                        taskCache.put(cacheKey, tasks)
                        _uiState.value = _uiState.value.copy(
                            tasks = tasks,
                            isLoading = false,
                            error = null
                        )
                        lastLoadTime = System.currentTimeMillis()
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            PerformanceMonitor.measureSuspendOperation("load_categories") {
                try {
                    val cachedCategories = categoryCache.get("categories")
                    if (cachedCategories != null) {
                        _uiState.value = _uiState.value.copy(categories = cachedCategories)
                        return@measureSuspendOperation
                    }
                    
                    taskRepository.getAllCategories().collect { categories ->
                        categoryCache.put("categories", categories)
                        _uiState.value = _uiState.value.copy(categories = categories)
                    }
                } catch (e: Exception) {
                    // Don't show error for categories, just log it
                    android.util.Log.w("TaskViewModel", "Failed to load categories", e)
                }
            }
        }
    }
    
    fun refreshTasks() {
        loadTasks(isRefresh = true)
    }
    
    fun loadMoreTasks() {
        if (_paginationState.value.isLoadingMore || !_paginationState.value.hasMoreItems) {
            return
        }
        
        viewModelScope.launch {
            _paginationState.value = _paginationState.value.copy(isLoadingMore = true)
            
            try {
                // Simulate pagination - in real app, you'd pass offset/limit to repository
                kotlinx.coroutines.delay(500) // Simulate network delay
                
                // For now, we'll just mark as no more items
                _paginationState.value = _paginationState.value.copy(
                    isLoadingMore = false,
                    hasMoreItems = false
                )
            } catch (e: Exception) {
                _paginationState.value = _paginationState.value.copy(
                    isLoadingMore = false,
                    error = e.message
                )
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
            PerformanceMonitor.measureSuspendOperation("add_task") {
                try {
                    val task = Task(
                        title = title,
                        description = description,
                        priority = priority,
                        category = category,
                        dueDate = dueDate
                    )
                    taskRepository.insertTask(task)
                    
                    // Clear cache to refresh data
                    taskCache.clear()
                    
                    _uiState.value = _uiState.value.copy(error = null)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            }
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            PerformanceMonitor.measureSuspendOperation("update_task") {
                try {
                    taskRepository.updateTask(task)
                    
                    // Clear cache to refresh data
                    taskCache.clear()
                    
                    _uiState.value = _uiState.value.copy(error = null)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            }
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            PerformanceMonitor.measureSuspendOperation("delete_task") {
                try {
                    taskRepository.deleteTask(task)
                    
                    // Clear cache to refresh data
                    taskCache.clear()
                    
                    _uiState.value = _uiState.value.copy(error = null)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            }
        }
    }
    
    fun toggleTaskCompletion(taskId: Long) {
        viewModelScope.launch {
            PerformanceMonitor.measureSuspendOperation("toggle_task_completion") {
                try {
                    taskRepository.toggleTaskCompletion(taskId)
                    
                    // Clear cache to refresh data
                    taskCache.clear()
                    
                    _uiState.value = _uiState.value.copy(error = null)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
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
        
        // Debounce search to avoid too many database queries
        viewModelScope.launch {
            kotlinx.coroutines.delay(300) // 300ms debounce
            if (newFilter.searchQuery == query) {
                loadTasks()
            }
        }
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
        _paginationState.value = _paginationState.value.copy(error = null)
    }
    
    suspend fun getTaskById(id: Long): Task? {
        return taskRepository.getTaskById(id)
    }
    
    fun getPerformanceReport(): String {
        return PerformanceMonitor.getPerformanceReport()
    }
    
    // Batch operations for better performance
    fun batchCompleteTasks(taskIds: List<Long>) {
        viewModelScope.launch {
            PerformanceMonitor.measureSuspendOperation("batch_complete_tasks") {
                try {
                    taskIds.forEach { taskId ->
                        taskRepository.toggleTaskCompletion(taskId)
                    }
                    taskCache.clear()
                    _uiState.value = _uiState.value.copy(error = null)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            }
        }
    }
    
    fun batchDeleteTasks(tasks: List<Task>) {
        viewModelScope.launch {
            PerformanceMonitor.measureSuspendOperation("batch_delete_tasks") {
                try {
                    tasks.forEach { task ->
                        taskRepository.deleteTask(task)
                    }
                    taskCache.clear()
                    _uiState.value = _uiState.value.copy(error = null)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        BackgroundTaskManager.cancelAll()
        taskCache.clear()
        categoryCache.clear()
    }
}

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class PaginationState(
    val isLoadingMore: Boolean = false,
    val hasMoreItems: Boolean = true,
    val error: String? = null
)