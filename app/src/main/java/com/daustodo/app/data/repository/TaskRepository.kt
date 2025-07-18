package com.daustodo.app.data.repository

import com.daustodo.app.data.database.TaskDao
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.Task
import com.daustodo.app.data.model.TaskFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    
    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    
    fun getActiveTasks(): Flow<List<Task>> = taskDao.getActiveTasks()
    
    fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    
    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)
    
    fun searchTasks(query: String): Flow<List<Task>> = taskDao.searchTasks(query)
    
    fun getTasksByCategory(category: String): Flow<List<Task>> = taskDao.getTasksByCategory(category)
    
    fun getTasksByPriority(priority: Priority): Flow<List<Task>> = taskDao.getTasksByPriority(priority)
    
    fun getAllCategories(): Flow<List<String>> = taskDao.getAllCategories()
    
    fun getFilteredTasks(filter: TaskFilter): Flow<List<Task>> {
        return combine(
            taskDao.getAllTasks(),
            getAllCategories()
        ) { tasks, _ ->
            tasks.filter { task ->
                // Filter by completion status
                val completionMatch = when {
                    filter.showCompleted && filter.showActive -> true
                    filter.showCompleted && !filter.showActive -> task.isCompleted
                    !filter.showCompleted && filter.showActive -> !task.isCompleted
                    else -> false
                }
                
                // Filter by category
                val categoryMatch = filter.category?.let { category ->
                    task.category.equals(category, ignoreCase = true)
                } ?: true
                
                // Filter by priority
                val priorityMatch = filter.priority?.let { priority ->
                    task.priority == priority
                } ?: true
                
                // Filter by search query
                val searchMatch = if (filter.searchQuery.isBlank()) {
                    true
                } else {
                    task.title.contains(filter.searchQuery, ignoreCase = true) ||
                    task.description.contains(filter.searchQuery, ignoreCase = true) ||
                    task.category.contains(filter.searchQuery, ignoreCase = true)
                }
                
                completionMatch && categoryMatch && priorityMatch && searchMatch
            }
        }
    }
    
    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.copy(updatedAt = LocalDateTime.now()))
    }
    
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
    
    suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
    }
    
    suspend fun toggleTaskCompletion(id: Long) {
        val task = taskDao.getTaskById(id)
        task?.let {
            taskDao.updateTaskCompletion(id, !it.isCompleted, LocalDateTime.now())
        }
    }
    
    suspend fun incrementPomodoroCount(id: Long) {
        taskDao.incrementPomodoroCount(id)
    }
}