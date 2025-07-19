package com.daustodo.app.data.database

import androidx.room.*
import com.daustodo.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO untuk operasi database Task dengan query yang jelas dan berguna
 */
@Dao
interface TaskDao {
    
    // ==================== CRUD OPERATIONS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long
    
    @Update
    suspend fun updateTask(task: Task)
    
    @Delete
    suspend fun deleteTask(task: Task)
    
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)
    
    // ==================== QUERY OPERATIONS ====================
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?
    
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<Task>>
    
    // ==================== FILTERED QUERIES ====================
    
    @Query("""
        SELECT * FROM tasks 
        WHERE isArchived = 0 
        AND (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        AND (:category IS NULL OR category = :category)
        AND (:priority IS NULL OR priority = :priority)
        AND (:isCompleted IS NULL OR isCompleted = :isCompleted)
        ORDER BY 
        CASE WHEN :sortBy = 'CREATED_AT' THEN createdAt END DESC,
        CASE WHEN :sortBy = 'DUE_DATE' THEN dueDate END ASC,
        CASE WHEN :sortBy = 'PRIORITY' THEN priority END DESC,
        CASE WHEN :sortBy = 'TITLE' THEN title END ASC,
        CASE WHEN :sortBy = 'PROGRESS' THEN progress END DESC,
        CASE WHEN :sortBy = 'POMODORO_COUNT' THEN pomodoroCount END DESC
    """)
    fun getFilteredTasks(
        searchQuery: String = "",
        category: String? = null,
        priority: Priority? = null,
        isCompleted: Boolean? = null,
        sortBy: String = "CREATED_AT"
    ): Flow<List<Task>>
    
    // ==================== STATUS-BASED QUERIES ====================
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isArchived = 0 ORDER BY dueDate ASC")
    fun getActiveTasksOrderedByDueDate(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 AND isArchived = 0 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>
    
    @Query("""
        SELECT * FROM tasks 
        WHERE isCompleted = 0 
        AND dueDate IS NOT NULL 
        AND dueDate < :now 
        AND isArchived = 0 
        ORDER BY dueDate ASC
    """)
    fun getOverdueTasks(now: LocalDateTime = LocalDateTime.now()): Flow<List<Task>>
    
    @Query("""
        SELECT * FROM tasks 
        WHERE DATE(createdAt) = DATE(:date) 
        AND isArchived = 0 
        ORDER BY createdAt DESC
    """)
    fun getTasksCreatedOn(date: LocalDateTime): Flow<List<Task>>
    
    @Query("""
        SELECT * FROM tasks 
        WHERE DATE(dueDate) = DATE(:date) 
        AND isCompleted = 0 
        AND isArchived = 0 
        ORDER BY dueDate ASC
    """)
    fun getTasksDueOn(date: LocalDateTime): Flow<List<Task>>
    
    // ==================== CATEGORY & TAG QUERIES ====================
    
    @Query("SELECT DISTINCT category FROM tasks WHERE category != '' AND isArchived = 0 ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT * FROM tasks WHERE category = :category AND isArchived = 0 ORDER BY createdAt DESC")
    fun getTasksByCategory(category: String): Flow<List<Task>>
    
    // ==================== PRIORITY QUERIES ====================
    
    @Query("SELECT * FROM tasks WHERE priority = :priority AND isArchived = 0 ORDER BY createdAt DESC")
    fun getTasksByPriority(priority: Priority): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE priority = 'URGENT' AND isCompleted = 0 AND isArchived = 0 ORDER BY dueDate ASC")
    fun getUrgentTasks(): Flow<List<Task>>
    
    // ==================== POMODORO QUERIES ====================
    
    @Query("SELECT * FROM tasks WHERE pomodoroCount > 0 AND isArchived = 0 ORDER BY pomodoroCount DESC")
    fun getTasksWithPomodoro(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE pomodoroCount < pomodoroGoal AND isCompleted = 0 AND isArchived = 0 ORDER BY pomodoroGoal - pomodoroCount DESC")
    fun getTasksNeedingPomodoro(): Flow<List<Task>>
    
    // ==================== STATISTICS QUERIES ====================
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isArchived = 0")
    suspend fun getTotalTaskCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND isArchived = 0")
    suspend fun getCompletedTaskCount(): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0 AND isArchived = 0")
    suspend fun getActiveTaskCount(): Int
    
    @Query("""
        SELECT COUNT(*) FROM tasks 
        WHERE isCompleted = 0 
        AND dueDate IS NOT NULL 
        AND dueDate < :now 
        AND isArchived = 0
    """)
    suspend fun getOverdueTaskCount(now: LocalDateTime = LocalDateTime.now()): Int
    
    @Query("SELECT AVG(actualMinutes) FROM tasks WHERE isCompleted = 1 AND actualMinutes > 0")
    suspend fun getAverageCompletionTime(): Float?
    
    @Query("SELECT SUM(pomodoroCount) FROM tasks WHERE isArchived = 0")
    suspend fun getTotalPomodoroSessions(): Int
    
    @Query("SELECT AVG(progress) FROM tasks WHERE isCompleted = 0 AND isArchived = 0")
    suspend fun getAverageProgress(): Float?
    
    // ==================== DATE RANGE QUERIES ====================
    
    @Query("""
        SELECT * FROM tasks 
        WHERE createdAt BETWEEN :startDate AND :endDate 
        AND isArchived = 0 
        ORDER BY createdAt DESC
    """)
    fun getTasksInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>>
    
    @Query("""
        SELECT * FROM tasks 
        WHERE completedAt BETWEEN :startDate AND :endDate 
        AND isCompleted = 1 
        AND isArchived = 0 
        ORDER BY completedAt DESC
    """)
    fun getTasksCompletedInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>>
    
    // ==================== RECURRING TASK QUERIES ====================
    
    @Query("SELECT * FROM tasks WHERE isRecurring = 1 AND isArchived = 0 ORDER BY createdAt DESC")
    fun getRecurringTasks(): Flow<List<Task>>
    
    // ==================== REMINDER QUERIES ====================
    
    @Query("""
        SELECT * FROM tasks 
        WHERE reminderEnabled = 1 
        AND reminderTime IS NOT NULL 
        AND reminderTime BETWEEN :startTime AND :endTime 
        AND isCompleted = 0 
        AND isArchived = 0 
        ORDER BY reminderTime ASC
    """)
    fun getTasksWithRemindersInRange(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<Task>>
    
    // ==================== BULK OPERATIONS ====================
    
    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean, completedAt: LocalDateTime? = null)
    
    @Query("UPDATE tasks SET pomodoroCount = pomodoroCount + 1 WHERE id = :taskId")
    suspend fun incrementPomodoroCount(taskId: Long)
    
    @Query("UPDATE tasks SET progress = :progress WHERE id = :taskId")
    suspend fun updateTaskProgress(taskId: Long, progress: Int)
    
    @Query("UPDATE tasks SET actualMinutes = actualMinutes + :minutes WHERE id = :taskId")
    suspend fun addActualMinutes(taskId: Long, minutes: Int)
    
    @Query("UPDATE tasks SET isArchived = 1 WHERE id = :taskId")
    suspend fun archiveTask(taskId: Long)
    
    @Query("UPDATE tasks SET isArchived = 0 WHERE id = :taskId")
    suspend fun unarchiveTask(taskId: Long)
    
    // ==================== SEARCH QUERIES ====================
    
    @Query("""
        SELECT * FROM tasks 
        WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND isArchived = 0 
        ORDER BY 
        CASE WHEN title LIKE :query || '%' THEN 1 ELSE 2 END,
        createdAt DESC
    """)
    fun searchTasks(query: String): Flow<List<Task>>
    
    // ==================== ANALYTICS QUERIES ====================
    
    @Query("""
        SELECT category, COUNT(*) as count 
        FROM tasks 
        WHERE isArchived = 0 
        GROUP BY category 
        ORDER BY count DESC
    """)
    suspend fun getCategoryDistribution(): List<CategoryCount>
    
    @Query("""
        SELECT priority, COUNT(*) as count 
        FROM tasks 
        WHERE isArchived = 0 
        GROUP BY priority 
        ORDER BY priority DESC
    """)
    suspend fun getPriorityDistribution(): List<PriorityCount>
    
    @Query("""
        SELECT DATE(createdAt) as date, COUNT(*) as count 
        FROM tasks 
        WHERE createdAt BETWEEN :startDate AND :endDate 
        AND isArchived = 0 
        GROUP BY DATE(createdAt) 
        ORDER BY date DESC
    """)
    suspend fun getTaskCreationTrend(startDate: LocalDateTime, endDate: LocalDateTime): List<DateCount>
}

/**
 * Data classes untuk analytics
 */
data class CategoryCount(
    val category: String,
    val count: Int
)

data class PriorityCount(
    val priority: Priority,
    val count: Int
)

data class DateCount(
    val date: String,
    val count: Int
)