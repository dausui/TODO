package com.daustodo.app.data.database

import androidx.room.*
import com.daustodo.app.data.model.Priority
import com.daustodo.app.data.model.Task
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
}