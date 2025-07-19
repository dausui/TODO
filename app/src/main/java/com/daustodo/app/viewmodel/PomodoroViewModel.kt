package com.daustodo.app.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daustodo.app.data.model.PomodoroState
import com.daustodo.app.data.model.PomodoroType
import com.daustodo.app.data.repository.PomodoroRepository
import com.daustodo.app.service.PomodoroService
import com.daustodo.app.ui.screens.pomodoro.PomodoroSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pomodoroRepository: PomodoroRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()
    
    private var pomodoroService: PomodoroService? = null
    private var bound = false
    
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PomodoroService.PomodoroServiceBinder
            pomodoroService = binder.getService()
            bound = true
            
            // Observe service state
            viewModelScope.launch {
                pomodoroService?.pomodoroState?.collect { state ->
                    _uiState.value = _uiState.value.copy(
                        pomodoroState = state,
                        isLoading = false
                    )
                    
                    // Update statistics when session completes
                    if (!state.isRunning && !state.isPaused && state.timeRemaining == 0) {
                        loadStatistics()
                    }
                }
            }
        }
        
        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            pomodoroService = null
        }
    }
    
    init {
        bindToService()
        loadStatistics()
    }
    
    private fun bindToService() {
        Intent(context, PomodoroService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val todayStats = pomodoroRepository.getCompletedWorkSessionsToday()
                val totalStats = pomodoroRepository.getTotalCompletedSessions()
                
                _uiState.value = _uiState.value.copy(
                    completedSessionsToday = todayStats,
                    totalSessionsCompleted = totalStats,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun startSession(taskId: Long? = null, type: PomodoroType = PomodoroType.WORK) {
        pomodoroService?.startSession(taskId, type)
    }
    
    fun startTimer() {
        pomodoroService?.startTimer()
    }
    
    fun pauseTimer() {
        pomodoroService?.pauseTimer()
    }
    
    fun stopTimer() {
        pomodoroService?.stopTimer()
    }
    
    fun skipSession() {
        pomodoroService?.skipSession()
    }
    
    fun resetSession() {
        val currentState = _uiState.value.pomodoroState
        val settings = _uiState.value.settings
        val defaultDuration = when (currentState.currentType) {
            PomodoroType.WORK -> settings.workDuration
            PomodoroType.SHORT_BREAK -> settings.shortBreakDuration
            PomodoroType.LONG_BREAK -> settings.longBreakDuration
        } * 60
        
        _uiState.value = _uiState.value.copy(
            pomodoroState = currentState.copy(
                timeRemaining = defaultDuration,
                isRunning = false,
                isPaused = false
            )
        )
        
        pomodoroService?.resetTimer(defaultDuration)
    }
    
    fun updateSettings(settings: PomodoroSettings) {
        _uiState.value = _uiState.value.copy(settings = settings)
        
        // Update service with new settings
        pomodoroService?.updateSettings(settings)
        
        // Reset current session if timer is not running
        val currentState = _uiState.value.pomodoroState
        if (!currentState.isRunning && !currentState.isPaused) {
            resetSession()
        }
    }
    
    fun setCustomDuration(minutes: Int) {
        val currentState = _uiState.value.pomodoroState
        _uiState.value = _uiState.value.copy(
            pomodoroState = currentState.copy(
                timeRemaining = minutes * 60
            )
        )
        
        pomodoroService?.setCustomDuration(minutes * 60)
    }
    
    fun resetToDefault() {
        resetSession()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
    }
}

data class PomodoroUiState(
    val pomodoroState: PomodoroState = PomodoroState(),
    val completedSessionsToday: Int = 0,
    val totalSessionsCompleted: Int = 0,
    val settings: PomodoroSettings = PomodoroSettings(),
    val isLoading: Boolean = true,
    val error: String? = null
)