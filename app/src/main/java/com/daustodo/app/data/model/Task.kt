package com.daustodo.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Model untuk tugas dengan fitur yang jelas dan berguna
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Informasi dasar tugas
    val title: String,
    val description: String = "",
    
    // Status dan prioritas
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    
    // Kategorisasi
    val category: String = "",
    val tags: List<String> = emptyList(),
    
    // Waktu dan deadline
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val dueDate: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    
    // Estimasi dan tracking
    val estimatedMinutes: Int = 0, // Estimasi waktu pengerjaan
    val actualMinutes: Int = 0,    // Waktu aktual yang digunakan
    
    // Pomodoro tracking
    val pomodoroCount: Int = 0,    // Jumlah sesi pomodoro yang selesai
    val pomodoroGoal: Int = 0,     // Target sesi pomodoro
    
    // Progress tracking
    val progress: Int = 0,         // Progress dalam persen (0-100)
    val subtasks: List<Subtask> = emptyList(),
    
    // Recurring task
    val isRecurring: Boolean = false,
    val recurringPattern: RecurringPattern? = null,
    
    // Notifications
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalDateTime? = null,
    
    // Metadata
    val lastModified: LocalDateTime = LocalDateTime.now(),
    val isArchived: Boolean = false
)

/**
 * Subtask untuk breakdown tugas besar
 */
data class Subtask(
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int = 0
)

/**
 * Prioritas tugas
 */
enum class Priority(val displayName: String, val color: String) {
    LOW("Rendah", "#4CAF50"),
    MEDIUM("Sedang", "#FF9800"),
    HIGH("Tinggi", "#F44336"),
    URGENT("Mendesak", "#9C27B0")
}

/**
 * Pola recurring task
 */
enum class RecurringPattern(val displayName: String, val days: Int) {
    DAILY("Setiap Hari", 1),
    WEEKLY("Setiap Minggu", 7),
    MONTHLY("Setiap Bulan", 30),
    CUSTOM("Kustom", 0)
}

/**
 * Status filter untuk tugas
 */
enum class TaskStatus(val displayName: String) {
    ALL("Semua"),
    ACTIVE("Aktif"),
    COMPLETED("Selesai"),
    OVERDUE("Terlambat"),
    TODAY("Hari Ini"),
    THIS_WEEK("Minggu Ini")
}

/**
 * Filter untuk pencarian dan sorting
 */
data class TaskFilter(
    val searchQuery: String = "",
    val status: TaskStatus = TaskStatus.ALL,
    val priority: Priority? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val dateRange: DateRange? = null,
    val sortBy: SortOption = SortOption.CREATED_AT,
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Range tanggal untuk filter
 */
data class DateRange(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

/**
 * Opsi sorting
 */
enum class SortOption(val displayName: String) {
    CREATED_AT("Tanggal Dibuat"),
    DUE_DATE("Deadline"),
    PRIORITY("Prioritas"),
    TITLE("Judul"),
    PROGRESS("Progress"),
    POMODORO_COUNT("Sesi Pomodoro")
}

/**
 * Urutan sorting
 */
enum class SortOrder(val displayName: String) {
    ASC("A-Z"),
    DESC("Z-A")
}

/**
 * Statistik tugas
 */
data class TaskStatistics(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val activeTasks: Int = 0,
    val overdueTasks: Int = 0,
    val completionRate: Float = 0f,
    val averageCompletionTime: Int = 0, // dalam menit
    val totalPomodoroSessions: Int = 0,
    val productivityScore: Float = 0f
)