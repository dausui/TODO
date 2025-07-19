package com.daustodo.app.data.repository

import com.daustodo.app.data.database.TaskDao
import com.daustodo.app.data.model.Task
import com.daustodo.app.data.model.TaskFilter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    
    fun getFilteredTasks(filter: TaskFilter): Flow<List<Task>> {
        return taskDao.getFilteredTasks(
            searchQuery = filter.searchQuery,
            category = filter.category,
            priority = filter.priority,
            showCompleted = filter.showCompleted,
            showActive = filter.showActive
        )
    }
    
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)
    }
    
    fun getAllCategories(): Flow<List<String>> {
        return taskDao.getAllCategories()
    }
    
    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
    
    suspend fun toggleTaskCompletion(taskId: Long) {
        taskDao.toggleTaskCompletion(taskId)
    }
    
    // Batch operations for better performance
    suspend fun batchInsertTasks(tasks: List<Task>): List<Long> {
        return taskDao.batchInsertTasks(tasks)
    }
    
    suspend fun batchUpdateTasks(tasks: List<Task>) {
        taskDao.batchUpdateTasks(tasks)
    }
    
    suspend fun batchDeleteTasks(taskIds: List<Long>) {
        taskDao.batchDeleteTasks(taskIds)
    }
    
    suspend fun batchToggleTaskCompletion(taskIds: List<Long>) {
        taskDao.batchToggleTaskCompletion(taskIds)
    }
    
    suspend fun incrementPomodoroCount(taskId: Long) {
        taskDao.incrementPomodoroCount(taskId)
    }
    

    
    suspend fun getOverdueTasks(): List<Task> {
        return taskDao.getOverdueTasks()
    }
    
    // Statistics for performance monitoring
    suspend fun getTaskCount(): Int {
        return taskDao.getTaskCount()
    }
    
    suspend fun getCompletedTaskCount(): Int {
        return taskDao.getCompletedTaskCount()
    }
    
    suspend fun getActiveTaskCount(): Int {
        return taskDao.getActiveTaskCount()
    }
    
    suspend fun getOverdueTaskCount(): Int {
        return taskDao.getOverdueTaskCount()
    }
    
    // Database maintenance
    suspend fun cleanupCompletedTasks(olderThanDays: Int = 30) {
        taskDao.cleanupCompletedTasks(olderThanDays)
    }
}