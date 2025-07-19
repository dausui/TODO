package com.daustodo.app.data.database

import androidx.room.*
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.Task
import com.daustodo.app.data.model.TaskFilter
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getActiveTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY updatedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?
    
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchTasks(query: String): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY createdAt DESC")
    fun getTasksByCategory(category: String): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY createdAt DESC")
    fun getTasksByPriority(priority: Priority): Flow<List<Task>>
    
    @Query("SELECT DISTINCT category FROM tasks WHERE category != '' ORDER BY category")
    fun getAllCategories(): Flow<List<String>>
    
    // Optimized filtered query with all filters in one query
    @Query("""
        SELECT * FROM tasks 
        WHERE (:searchQuery IS NULL OR :searchQuery = '' OR 
               title LIKE '%' || :searchQuery || '%' OR 
               description LIKE '%' || :searchQuery || '%' OR 
               category LIKE '%' || :searchQuery || '%')
        AND (:category IS NULL OR category = :category)
        AND (:priority IS NULL OR priority = :priority)
        AND (:showCompleted = 1 OR isCompleted = 0)
        AND (:showActive = 1 OR isCompleted = 1)
        ORDER BY 
            CASE 
                WHEN isCompleted = 0 THEN 0 
                ELSE 1 
            END,
            priority DESC,
            createdAt DESC
    """)
    fun getFilteredTasks(
        searchQuery: String? = null,
        category: String? = null,
        priority: Priority? = null,
        showCompleted: Boolean = true,
        showActive: Boolean = true
    ): Flow<List<Task>>
    
    @Insert
    suspend fun insertTask(task: Task): Long
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)
    
    @Query("UPDATE tasks SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, isCompleted: Boolean, updatedAt: java.time.LocalDateTime)
    
    @Query("UPDATE tasks SET pomodoroCount = pomodoroCount + 1 WHERE id = :id")
    suspend fun incrementPomodoroCount(id: Long)
    
    // Batch operations for better performance
    @Insert
    suspend fun batchInsertTasks(tasks: List<Task>): List<Long>
    
    @Update
    suspend fun batchUpdateTasks(tasks: List<Task>)
    
    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    suspend fun batchDeleteTasks(taskIds: List<Long>)
    
    @Query("UPDATE tasks SET isCompleted = NOT isCompleted, updatedAt = :updatedAt WHERE id IN (:taskIds)")
    suspend fun batchToggleTaskCompletion(taskIds: List<Long>, updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now())
    

    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND dueDate < :now ORDER BY dueDate ASC")
    suspend fun getOverdueTasks(now: java.time.LocalDateTime = java.time.LocalDateTime.now()): List<Task>
    
    // Statistics for performance monitoring
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    suspend fun getCompletedTaskCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    suspend fun getActiveTaskCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND dueDate < :now")
    suspend fun getOverdueTaskCount(now: java.time.LocalDateTime = java.time.LocalDateTime.now()): Int
    
    // Database maintenance and optimization
    @Query("DELETE FROM tasks WHERE isCompleted = 1 AND updatedAt < datetime('now', '-' || :days || ' days')")
    suspend fun cleanupCompletedTasks(days: Int = 30)
    
    // Toggle task completion with current timestamp
    @Query("UPDATE tasks SET isCompleted = NOT isCompleted, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun toggleTaskCompletion(taskId: Long, updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now())
}