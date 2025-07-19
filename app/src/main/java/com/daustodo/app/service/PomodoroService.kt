package com.daustodo.app.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.daustodo.app.R
import com.daustodo.app.data.model.PomodoroState
import com.daustodo.app.data.model.PomodoroType
import com.daustodo.app.data.repository.PomodoroRepository
import com.daustodo.app.data.repository.TaskRepository
import com.daustodo.app.ui.screens.pomodoro.PomodoroSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class PomodoroService : Service() {
    
    @Inject
    lateinit var pomodoroRepository: PomodoroRepository
    
    @Inject
    lateinit var taskRepository: TaskRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    
    private val _pomodoroState = MutableStateFlow(
        PomodoroState(
            timeRemaining = 25 * 60, // 25 minutes default
            currentType = PomodoroType.WORK,
            isRunning = false,
            isPaused = false,
            currentSession = 1,
            totalSessions = 4
        )
    )
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    
    private var currentTaskId: Long? = null
    private var settings = PomodoroSettings()
    private var customDuration: Int? = null
    
    private val binder = PomodoroServiceBinder()
    
    inner class PomodoroServiceBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PomodoroService created")
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "PomodoroService destroyed")
        timerJob?.cancel()
        serviceScope.cancel()
    }
    
    fun startSession(taskId: Long?, type: PomodoroType = PomodoroType.WORK) {
        try {
            currentTaskId = taskId
            val duration = when (type) {
                PomodoroType.WORK -> settings.workDuration
                PomodoroType.SHORT_BREAK -> settings.shortBreakDuration
                PomodoroType.LONG_BREAK -> settings.longBreakDuration
            } * 60
            
            _pomodoroState.value = _pomodoroState.value.copy(
                timeRemaining = duration,
                currentType = type,
                isRunning = false,
                isPaused = false
            )
            
            Log.d(TAG, "Started session: $type with duration: $duration seconds")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting session", e)
        }
    }
    
    fun startTimer() {
        try {
            if (_pomodoroState.value.isRunning) {
                Log.w(TAG, "Timer is already running")
                return
            }
            
            _pomodoroState.value = _pomodoroState.value.copy(
                isRunning = true,
                isPaused = false
            )
            
            startForeground(NOTIFICATION_ID, createNotification())
            
            timerJob = serviceScope.launch {
                while (_pomodoroState.value.timeRemaining > 0 && _pomodoroState.value.isRunning) {
                    delay(1000)
                    _pomodoroState.value = _pomodoroState.value.copy(
                        timeRemaining = _pomodoroState.value.timeRemaining - 1
                    )
                    
                    // Update notification
                    updateNotification()
                }
                
                if (_pomodoroState.value.timeRemaining <= 0) {
                    onSessionComplete()
                }
            }
            
            Log.d(TAG, "Timer started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting timer", e)
            _pomodoroState.value = _pomodoroState.value.copy(
                isRunning = false,
                isPaused = false
            )
        }
    }
    
    fun pauseTimer() {
        try {
            if (!_pomodoroState.value.isRunning) {
                Log.w(TAG, "Timer is not running")
                return
            }
            
            _pomodoroState.value = _pomodoroState.value.copy(
                isRunning = false,
                isPaused = true
            )
            
            timerJob?.cancel()
            updateNotification()
            
            Log.d(TAG, "Timer paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing timer", e)
        }
    }
    
    fun stopTimer() {
        try {
            _pomodoroState.value = _pomodoroState.value.copy(
                isRunning = false,
                isPaused = false
            )
            
            timerJob?.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            
            Log.d(TAG, "Timer stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping timer", e)
        }
    }
    
    fun skipSession() {
        try {
            timerJob?.cancel()
            onSessionComplete()
            
            Log.d(TAG, "Session skipped")
        } catch (e: Exception) {
            Log.e(TAG, "Error skipping session", e)
        }
    }
    
    fun resetTimer(duration: Int) {
        try {
            timerJob?.cancel()
            _pomodoroState.value = _pomodoroState.value.copy(
                timeRemaining = duration,
                isRunning = false,
                isPaused = false
            )
            
            updateNotification()
            
            Log.d(TAG, "Timer reset to $duration seconds")
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting timer", e)
        }
    }
    
    fun setCustomDuration(seconds: Int) {
        try {
            if (seconds <= 0) {
                Log.w(TAG, "Invalid custom duration: $seconds")
                return
            }
            
            customDuration = seconds
            _pomodoroState.value = _pomodoroState.value.copy(
                timeRemaining = seconds
            )
            
            Log.d(TAG, "Custom duration set to $seconds seconds")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting custom duration", e)
        }
    }
    
    fun updateSettings(newSettings: PomodoroSettings) {
        try {
            settings = newSettings
            
            // Reset timer if not running
            if (!_pomodoroState.value.isRunning && !_pomodoroState.value.isPaused) {
                val duration = when (_pomodoroState.value.currentType) {
                    PomodoroType.WORK -> settings.workDuration
                    PomodoroType.SHORT_BREAK -> settings.shortBreakDuration
                    PomodoroType.LONG_BREAK -> settings.longBreakDuration
                } * 60
                
                _pomodoroState.value = _pomodoroState.value.copy(
                    timeRemaining = duration
                )
            }
            
            Log.d(TAG, "Settings updated")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating settings", e)
        }
    }
    
    private fun onSessionComplete() {
        try {
            _pomodoroState.value = _pomodoroState.value.copy(
                isRunning = false,
                isPaused = false
            )
            
            timerJob?.cancel()
            
            // Save session completion
            serviceScope.launch {
                try {
                    val session = com.daustodo.app.data.model.PomodoroSession(
                        taskId = currentTaskId,
                        type = _pomodoroState.value.currentType,
                        completedAt = LocalDateTime.now(),
                        duration = when (_pomodoroState.value.currentType) {
                            PomodoroType.WORK -> settings.workDuration
                            PomodoroType.SHORT_BREAK -> settings.shortBreakDuration
                            PomodoroType.LONG_BREAK -> settings.longBreakDuration
                        }
                    )
                    
                    pomodoroRepository.insertSession(session)
                    
                    // Increment task pomodoro count if it's a work session
                    if (_pomodoroState.value.currentType == PomodoroType.WORK && currentTaskId != null) {
                        taskRepository.incrementPomodoroCount(currentTaskId!!)
                    }
                    
                    Log.d(TAG, "Session completed and saved")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving session completion", e)
                }
            }
            
            // Determine next session type
            val currentSession = _pomodoroState.value.currentSession
            val nextType = when {
                _pomodoroState.value.currentType == PomodoroType.WORK -> {
                    if (currentSession % settings.sessionsBeforeLongBreak == 0) {
                        PomodoroType.LONG_BREAK
                    } else {
                        PomodoroType.SHORT_BREAK
                    }
                }
                else -> PomodoroType.WORK
            }
            
            val nextSession = if (nextType == PomodoroType.WORK) {
                currentSession + 1
            } else {
                currentSession
            }
            
            // Start next session
            startSession(currentTaskId, nextType)
            _pomodoroState.value = _pomodoroState.value.copy(
                currentSession = nextSession
            )
            
            updateNotification()
            
            Log.d(TAG, "Next session prepared: $nextType")
        } catch (e: Exception) {
            Log.e(TAG, "Error completing session", e)
        }
    }
    
    private fun createNotificationChannel() {
        try {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows Pomodoro timer progress"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification channel", e)
        }
    }
    
    private fun createNotification(): Notification {
        try {
            val state = _pomodoroState.value
            val timeString = formatTime(state.timeRemaining)
            val typeString = when (state.currentType) {
                PomodoroType.WORK -> "Kerja"
                PomodoroType.SHORT_BREAK -> "Istirahat Pendek"
                PomodoroType.LONG_BREAK -> "Istirahat Panjang"
            }
            
            val title = if (state.isRunning) {
                "$typeString - $timeString"
            } else if (state.isPaused) {
                "$typeString - Dijeda"
            } else {
                "$typeString - Selesai"
            }
            
            val content = when (state.currentType) {
                PomodoroType.WORK -> "Fokus pada tugas Anda"
                PomodoroType.SHORT_BREAK -> "Ambil istirahat sebentar"
                PomodoroType.LONG_BREAK -> "Istirahat yang lebih panjang"
            }
            
            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_timer)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification", e)
            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Pomodoro Timer")
                .setContentText("Timer sedang berjalan")
                .setSmallIcon(R.drawable.ic_timer)
                .build()
        }
    }
    
    private fun updateNotification() {
        try {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification", e)
        }
    }
    
    private fun formatTime(seconds: Int): String {
        return try {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            String.format("%02d:%02d", minutes, remainingSeconds)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting time", e)
            "00:00"
        }
    }
    
    companion object {
        private const val TAG = "PomodoroService"
        private const val CHANNEL_ID = "pomodoro_timer"
        private const val NOTIFICATION_ID = 1001
    }
}