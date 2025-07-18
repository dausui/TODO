package com.daustodo.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val type: PomodoroType = PomodoroType.WORK,
    val duration: Int, // in minutes
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val isCompleted: Boolean = false
)

enum class PomodoroType(val displayName: String, val defaultDuration: Int) {
    WORK("Work", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15)
}

data class PomodoroState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val currentType: PomodoroType = PomodoroType.WORK,
    val timeRemaining: Int = 0, // in seconds
    val currentSession: Int = 1,
    val totalSessions: Int = 4,
    val linkedTaskId: Long? = null
)