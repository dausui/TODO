package com.daustodo.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.daustodo.app.MainActivity
import com.daustodo.app.R
import com.daustodo.app.data.model.PomodoroState
import com.daustodo.app.data.model.PomodoroType
import com.daustodo.app.data.repository.PomodoroRepository
import com.daustodo.app.data.repository.TaskRepository
import com.daustodo.app.utils.SoundManager
import com.daustodo.app.utils.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import android.app.Notification

@AndroidEntryPoint
class PomodoroService : Service() {
    
    @Inject
    lateinit var soundManager: SoundManager
    
    @Inject
    lateinit var pomodoroRepository: PomodoroRepository
    
    @Inject
    lateinit var taskRepository: TaskRepository
    
    private val binder = PomodoroServiceBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    
    private var currentSessionId: Long? = null
    
    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "pomodoro_channel"
        const val ACTION_START = "action_start"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_STOP = "action_stop"
        const val ACTION_SKIP = "action_skip"
    }
    
    inner class PomodoroServiceBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTimer()
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimer()
            ACTION_SKIP -> skipSession()
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Pomodoro timer notifications"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DausTodo::PomodoroWakeLock"
        )
        wakeLock?.acquire(60 * 60 * 1000L) // 1 hour max
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let { wl ->
            if (wl.isHeld) {
                wl.release()
            }
        }
    }
    
    fun startSession(taskId: Long?, type: PomodoroType = PomodoroType.WORK) {
        serviceScope.launch {
            currentSessionId = pomodoroRepository.startNewSession(taskId, type)
            _pomodoroState.value = _pomodoroState.value.copy(
                currentType = type,
                timeRemaining = TimeUtils.minutesToSeconds(type.defaultDuration),
                linkedTaskId = taskId,
                isRunning = false,
                isPaused = false
            )
            updateNotification()
        }
    }
    
    fun startTimer() {
        if (_pomodoroState.value.isRunning) return
        
        _pomodoroState.value = _pomodoroState.value.copy(
            isRunning = true,
            isPaused = false
        )
        
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (_pomodoroState.value.isRunning && _pomodoroState.value.timeRemaining > 0) {
                delay(1000)
                _pomodoroState.value = _pomodoroState.value.copy(
                    timeRemaining = _pomodoroState.value.timeRemaining - 1
                )
                updateNotification()
            }
            
            if (_pomodoroState.value.timeRemaining <= 0) {
                serviceScope.launch {
                    completeSession()
                }
            }
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    fun pauseTimer() {
        _pomodoroState.value = _pomodoroState.value.copy(
            isRunning = false,
            isPaused = true
        )
        timerJob?.cancel()
        updateNotification()
    }
    
    fun stopTimer() {
        _pomodoroState.value = _pomodoroState.value.copy(
            isRunning = false,
            isPaused = false,
            timeRemaining = TimeUtils.minutesToSeconds(_pomodoroState.value.currentType.defaultDuration)
        )
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    fun skipSession() {
        serviceScope.launch {
            completeSession()
        }
    }
    
    private suspend fun completeSession() {
        currentSessionId?.let { sessionId ->
            pomodoroRepository.completeSession(sessionId)
            
            // Increment pomodoro count for linked task
            _pomodoroState.value.linkedTaskId?.let { taskId ->
                if (_pomodoroState.value.currentType == PomodoroType.WORK) {
                    taskRepository.incrementPomodoroCount(taskId)
                }
            }
        }
        
        // Play completion sound
        when (_pomodoroState.value.currentType) {
            PomodoroType.WORK -> {
                soundManager.playWorkCompleteSound()
                showCompletionNotification("Work session completed! Time for a break.")
            }
            PomodoroType.SHORT_BREAK, PomodoroType.LONG_BREAK -> {
                soundManager.playBreakCompleteSound()
                showCompletionNotification("Break completed! Ready to work?")
            }
        }
        
        // Auto-switch to next session type
        val nextType = getNextSessionType()
        val nextSession = if (_pomodoroState.value.currentType == PomodoroType.WORK) {
            _pomodoroState.value.currentSession + 1
        } else {
            _pomodoroState.value.currentSession
        }
        
        _pomodoroState.value = _pomodoroState.value.copy(
            isRunning = false,
            isPaused = false,
            currentType = nextType,
            timeRemaining = TimeUtils.minutesToSeconds(nextType.defaultDuration),
            currentSession = nextSession
        )
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    private fun getNextSessionType(): PomodoroType {
        return when (_pomodoroState.value.currentType) {
            PomodoroType.WORK -> {
                if (_pomodoroState.value.currentSession % 4 == 0) {
                    PomodoroType.LONG_BREAK
                } else {
                    PomodoroType.SHORT_BREAK
                }
            }
            PomodoroType.SHORT_BREAK, PomodoroType.LONG_BREAK -> PomodoroType.WORK
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val state = _pomodoroState.value
        val timeText = TimeUtils.formatTime(state.timeRemaining)
        val title = "${state.currentType.displayName} - $timeText"
        val content = if (state.linkedTaskId != null) {
            "Working on task • Session ${state.currentSession}/${state.totalSessions}"
        } else {
            "Session ${state.currentSession}/${state.totalSessions}"
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(createNotificationAction())
            .build()
    }
    
    private fun createNotificationAction(): NotificationCompat.Action {
        val actionIntent = Intent(this, PomodoroService::class.java)
        val actionText: String
        val action: String
        
        when {
            _pomodoroState.value.isRunning -> {
                actionText = "Pause"
                action = ACTION_PAUSE
            }
            _pomodoroState.value.isPaused -> {
                actionText = "Resume"
                action = ACTION_START
            }
            else -> {
                actionText = "Start"
                action = ACTION_START
            }
        }
        
        actionIntent.action = action
        val pendingIntent = PendingIntent.getService(
            this, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action(
            R.drawable.ic_play_pause,
            actionText,
            pendingIntent
        )
    }
    
    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
    
    private fun showCompletionNotification(message: String) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pomodoro Complete")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_check)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
}