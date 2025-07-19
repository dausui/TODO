package com.daustodo.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Model untuk sesi Pomodoro dengan tracking yang jelas
 */
@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Referensi tugas
    val taskId: Long? = null,
    
    // Jenis sesi
    val type: PomodoroType = PomodoroType.WORK,
    
    // Waktu dan durasi
    val startedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val duration: Int = 25, // dalam menit
    val actualDuration: Int = 0, // durasi aktual dalam menit
    
    // Status sesi
    val status: SessionStatus = SessionStatus.ACTIVE,
    val isCompleted: Boolean = false,
    
    // Interupsi dan jeda
    val interruptions: Int = 0,
    val pauseDuration: Int = 0, // total waktu jeda dalam menit
    
    // Notes dan feedback
    val notes: String = "",
    val productivityRating: Int = 0, // 1-5 rating
    
    // Metadata
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Jenis sesi Pomodoro
 */
enum class PomodoroType(val displayName: String, val defaultDuration: Int) {
    WORK("Kerja", 25),
    SHORT_BREAK("Istirahat Pendek", 5),
    LONG_BREAK("Istirahat Panjang", 15),
    CUSTOM("Kustom", 25)
}

/**
 * Status sesi
 */
enum class SessionStatus(val displayName: String) {
    ACTIVE("Aktif"),
    PAUSED("Dijeda"),
    COMPLETED("Selesai"),
    INTERRUPTED("Terinterupsi"),
    CANCELLED("Dibatalkan")
}

/**
 * State Pomodoro untuk UI
 */
data class PomodoroState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val timeRemaining: Int = 25 * 60, // dalam detik
    val totalTime: Int = 25 * 60, // total waktu dalam detik
    val currentType: PomodoroType = PomodoroType.WORK,
    val currentSession: Int = 1,
    val totalSessions: Int = 4,
    val currentTaskId: Long? = null,
    val currentTaskTitle: String = "",
    val progress: Float = 0f, // 0.0 - 1.0
    val sessionStartTime: LocalDateTime? = null,
    val interruptions: Int = 0
)

/**
 * Pengaturan Pomodoro
 */
data class PomodoroSettings(
    val workDuration: Int = 25, // menit
    val shortBreakDuration: Int = 5, // menit
    val longBreakDuration: Int = 15, // menit
    val sessionsBeforeLongBreak: Int = 4,
    val autoStartBreaks: Boolean = false,
    val autoStartWork: Boolean = false,
    val soundEnabled: Boolean = true,
    val notificationEnabled: Boolean = true,
    val strictMode: Boolean = false, // tidak bisa skip sesi
    val customDuration: Int = 25 // untuk sesi kustom
)

/**
 * Statistik Pomodoro
 */
data class PomodoroStatistics(
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val totalWorkTime: Int = 0, // dalam menit
    val totalBreakTime: Int = 0, // dalam menit
    val averageSessionLength: Int = 0, // dalam menit
    val completionRate: Float = 0f, // 0.0 - 1.0
    val todaySessions: Int = 0,
    val thisWeekSessions: Int = 0,
    val thisMonthSessions: Int = 0,
    val productivityScore: Float = 0f, // 0.0 - 100.0
    val longestStreak: Int = 0, // hari berturut-turut
    val currentStreak: Int = 0
)

/**
 * Interupsi dalam sesi Pomodoro
 */
data class PomodoroInterruption(
    val id: Long = 0,
    val sessionId: Long,
    val reason: String,
    val duration: Int, // dalam detik
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Goal dan target Pomodoro
 */
data class PomodoroGoal(
    val id: Long = 0,
    val title: String,
    val targetSessions: Int,
    val currentSessions: Int = 0,
    val deadline: LocalDateTime? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Timer state untuk UI
 */
data class TimerState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val timeRemaining: Int = 0,
    val totalTime: Int = 0,
    val progress: Float = 0f,
    val currentPhase: String = "",
    val nextPhase: String = "",
    val canSkip: Boolean = false,
    val canPause: Boolean = true
)