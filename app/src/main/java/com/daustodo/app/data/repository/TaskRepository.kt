package com.daustodo.app.data.repository

import com.daustodo.app.data.database.TaskDao
import com.daustodo.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk operasi Task dengan logika yang jelas dan fitur yang berguna
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    
    // ==================== CRUD OPERATIONS ====================
    
    /**
     * Menambah tugas baru
     */
    suspend fun addTask(task: Task): Long {
        return taskDao.insertTask(task)
    }
    
    /**
     * Memperbarui tugas
     */
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
    
    /**
     * Menghapus tugas
     */
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
    
    /**
     * Menghapus tugas berdasarkan ID
     */
    suspend fun deleteTaskById(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }
    
    /**
     * Mendapatkan tugas berdasarkan ID
     */
    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)
    }
    
    // ==================== QUERY OPERATIONS ====================
    
    /**
     * Mendapatkan semua tugas
     */
    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }
    
    /**
     * Mendapatkan tugas aktif (tidak diarsipkan)
     */
    fun getActiveTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks()
    }
    
    /**
     * Mendapatkan tugas dengan filter
     */
    fun getFilteredTasks(filter: TaskFilter): Flow<List<Task>> {
        return taskDao.getFilteredTasks(
            searchQuery = filter.searchQuery,
            category = filter.category,
            priority = filter.priority,
            isCompleted = when (filter.status) {
                TaskStatus.ALL -> null
                TaskStatus.ACTIVE -> false
                TaskStatus.COMPLETED -> true
                TaskStatus.OVERDUE -> false
                TaskStatus.TODAY -> false
                TaskStatus.THIS_WEEK -> false
            },
            sortBy = filter.sortBy.name
        )
    }
    
    // ==================== STATUS-BASED QUERIES ====================
    
    /**
     * Mendapatkan tugas aktif yang diurutkan berdasarkan deadline
     */
    fun getActiveTasksOrderedByDueDate(): Flow<List<Task>> {
        return taskDao.getActiveTasksOrderedByDueDate()
    }
    
    /**
     * Mendapatkan tugas yang sudah selesai
     */
    fun getCompletedTasks(): Flow<List<Task>> {
        return taskDao.getCompletedTasks()
    }
    
    /**
     * Mendapatkan tugas yang terlambat
     */
    fun getOverdueTasks(): Flow<List<Task>> {
        return taskDao.getOverdueTasks()
    }
    
    /**
     * Mendapatkan tugas yang dibuat pada tanggal tertentu
     */
    fun getTasksCreatedOn(date: LocalDateTime): Flow<List<Task>> {
        return taskDao.getTasksCreatedOn(date)
    }
    
    /**
     * Mendapatkan tugas yang deadline pada tanggal tertentu
     */
    fun getTasksDueOn(date: LocalDateTime): Flow<List<Task>> {
        return taskDao.getTasksDueOn(date)
    }
    
    // ==================== CATEGORY & PRIORITY QUERIES ====================
    
    /**
     * Mendapatkan semua kategori
     */
    fun getAllCategories(): Flow<List<String>> {
        return taskDao.getAllCategories()
    }
    
    /**
     * Mendapatkan tugas berdasarkan kategori
     */
    fun getTasksByCategory(category: String): Flow<List<Task>> {
        return taskDao.getTasksByCategory(category)
    }
    
    /**
     * Mendapatkan tugas berdasarkan prioritas
     */
    fun getTasksByPriority(priority: Priority): Flow<List<Task>> {
        return taskDao.getTasksByPriority(priority)
    }
    
    /**
     * Mendapatkan tugas urgent
     */
    fun getUrgentTasks(): Flow<List<Task>> {
        return taskDao.getUrgentTasks()
    }
    
    // ==================== POMODORO QUERIES ====================
    
    /**
     * Mendapatkan tugas yang sudah menggunakan Pomodoro
     */
    fun getTasksWithPomodoro(): Flow<List<Task>> {
        return taskDao.getTasksWithPomodoro()
    }
    
    /**
     * Mendapatkan tugas yang membutuhkan Pomodoro
     */
    fun getTasksNeedingPomodoro(): Flow<List<Task>> {
        return taskDao.getTasksNeedingPomodoro()
    }
    
    // ==================== SEARCH OPERATIONS ====================
    
    /**
     * Mencari tugas berdasarkan query
     */
    fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(query)
    }
    
    // ==================== BULK OPERATIONS ====================
    
    /**
     * Mengubah status penyelesaian tugas
     */
    suspend fun toggleTaskCompletion(taskId: Long) {
        val task = getTaskById(taskId)
        task?.let {
            val isCompleted = !it.isCompleted
            val completedAt = if (isCompleted) LocalDateTime.now() else null
            taskDao.updateTaskCompletion(taskId, isCompleted, completedAt)
        }
    }
    
    /**
     * Menambah jumlah sesi Pomodoro
     */
    suspend fun incrementPomodoroCount(taskId: Long) {
        taskDao.incrementPomodoroCount(taskId)
    }
    
    /**
     * Memperbarui progress tugas
     */
    suspend fun updateTaskProgress(taskId: Long, progress: Int) {
        taskDao.updateTaskProgress(taskId, progress)
    }
    
    /**
     * Menambah waktu aktual pengerjaan
     */
    suspend fun addActualMinutes(taskId: Long, minutes: Int) {
        taskDao.addActualMinutes(taskId, minutes)
    }
    
    /**
     * Mengarsipkan tugas
     */
    suspend fun archiveTask(taskId: Long) {
        taskDao.archiveTask(taskId)
    }
    
    /**
     * Membatalkan arsip tugas
     */
    suspend fun unarchiveTask(taskId: Long) {
        taskDao.unarchiveTask(taskId)
    }
    
    // ==================== STATISTICS OPERATIONS ====================
    
    /**
     * Mendapatkan statistik tugas
     */
    suspend fun getTaskStatistics(): TaskStatistics {
        val totalTasks = taskDao.getTotalTaskCount()
        val completedTasks = taskDao.getCompletedTaskCount()
        val activeTasks = taskDao.getActiveTaskCount()
        val overdueTasks = taskDao.getOverdueTaskCount()
        val averageCompletionTime = taskDao.getAverageCompletionTime()?.toInt() ?: 0
        val totalPomodoroSessions = taskDao.getTotalPomodoroSessions()
        val averageProgress = taskDao.getAverageProgress() ?: 0f
        
        val completionRate = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
        val productivityScore = calculateProductivityScore(completionRate, averageProgress, totalPomodoroSessions)
        
        return TaskStatistics(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            activeTasks = activeTasks,
            overdueTasks = overdueTasks,
            completionRate = completionRate,
            averageCompletionTime = averageCompletionTime,
            totalPomodoroSessions = totalPomodoroSessions,
            productivityScore = productivityScore
        )
    }
    
    /**
     * Mendapatkan distribusi kategori
     */
    suspend fun getCategoryDistribution(): List<CategoryCount> {
        return taskDao.getCategoryDistribution()
    }
    
    /**
     * Mendapatkan distribusi prioritas
     */
    suspend fun getPriorityDistribution(): List<PriorityCount> {
        return taskDao.getPriorityDistribution()
    }
    
    /**
     * Mendapatkan tren pembuatan tugas
     */
    suspend fun getTaskCreationTrend(startDate: LocalDateTime, endDate: LocalDateTime): List<DateCount> {
        return taskDao.getTaskCreationTrend(startDate, endDate)
    }
    
    // ==================== DATE RANGE OPERATIONS ====================
    
    /**
     * Mendapatkan tugas dalam rentang tanggal
     */
    fun getTasksInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>> {
        return taskDao.getTasksInDateRange(startDate, endDate)
    }
    
    /**
     * Mendapatkan tugas yang selesai dalam rentang tanggal
     */
    fun getTasksCompletedInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Task>> {
        return taskDao.getTasksCompletedInDateRange(startDate, endDate)
    }
    
    // ==================== RECURRING & REMINDER OPERATIONS ====================
    
    /**
     * Mendapatkan tugas berulang
     */
    fun getRecurringTasks(): Flow<List<Task>> {
        return taskDao.getRecurringTasks()
    }
    
    /**
     * Mendapatkan tugas dengan pengingat dalam rentang waktu
     */
    fun getTasksWithRemindersInRange(startTime: LocalDateTime, endTime: LocalDateTime): Flow<List<Task>> {
        return taskDao.getTasksWithRemindersInRange(startTime, endTime)
    }
    
    // ==================== UTILITY OPERATIONS ====================
    
    /**
     * Menghitung skor produktivitas
     */
    private fun calculateProductivityScore(
        completionRate: Float,
        averageProgress: Float,
        totalPomodoroSessions: Int
    ): Float {
        val completionScore = completionRate * 40 // 40% weight
        val progressScore = (averageProgress / 100f) * 30 // 30% weight
        val pomodoroScore = minOf(totalPomodoroSessions * 2f, 30f) // 30% weight, max 30
        
        return completionScore + progressScore + pomodoroScore
    }
    
    /**
     * Mendapatkan tugas untuk hari ini
     */
    fun getTodayTasks(): Flow<List<Task>> {
        val today = LocalDateTime.now()
        return taskDao.getTasksDueOn(today)
    }
    
    /**
     * Mendapatkan tugas untuk minggu ini
     */
    fun getThisWeekTasks(): Flow<List<Task>> {
        val now = LocalDateTime.now()
        val startOfWeek = now.toLocalDate().minusDays(now.dayOfWeek.value.toLong() - 1).atStartOfDay()
        val endOfWeek = startOfWeek.plusDays(7).minusSeconds(1)
        return taskDao.getTasksInDateRange(startOfWeek, endOfWeek)
    }
    
    /**
     * Mendapatkan tugas yang membutuhkan perhatian (urgent + overdue)
     */
    fun getTasksNeedingAttention(): Flow<List<Task>> {
        // Combine urgent and overdue tasks
        return taskDao.getUrgentTasks()
    }
}