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
            try {
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Gagal terhubung ke layanan Pomodoro: ${e.message}",
                    isLoading = false
                )
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
        try {
            Intent(context, PomodoroService::class.java).also { intent ->
                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal memulai layanan Pomodoro: ${e.message}",
                isLoading = false
            )
        }
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val todayStats = pomodoroRepository.getCompletedWorkSessionsToday()
                val totalStats = pomodoroRepository.getTotalCompletedSessions()
                
                _uiState.value = _uiState.value.copy(
                    completedSessionsToday = todayStats,
                    totalSessionsCompleted = totalStats,
                    error = null,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Gagal memuat statistik: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun startSession(taskId: Long?, type: PomodoroType = PomodoroType.WORK) {
        try {
            if (pomodoroService != null) {
                pomodoroService?.startSession(taskId, type)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Layanan Pomodoro belum siap"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal memulai sesi: ${e.message}"
            )
        }
    }
    
    fun startTimer() {
        try {
            if (pomodoroService != null) {
                pomodoroService?.startTimer()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = "Layanan Pomodoro belum siap"
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal memulai timer: ${e.message}"
            )
        }
    }
    
    fun pauseTimer() {
        try {
            pomodoroService?.pauseTimer()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal menjeda timer: ${e.message}"
            )
        }
    }
    
    fun stopTimer() {
        try {
            pomodoroService?.stopTimer()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal menghentikan timer: ${e.message}"
            )
        }
    }
    
    fun skipSession() {
        try {
            pomodoroService?.skipSession()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal melewati sesi: ${e.message}"
            )
        }
    }
    
    fun resetSession() {
        try {
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
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal mereset sesi: ${e.message}"
            )
        }
    }
    
    fun updateSettings(settings: PomodoroSettings) {
        try {
            _uiState.value = _uiState.value.copy(settings = settings)
            
            // Update service with new settings
            pomodoroService?.updateSettings(settings)
            
            // Reset current session if timer is not running
            val currentState = _uiState.value.pomodoroState
            if (!currentState.isRunning && !currentState.isPaused) {
                resetSession()
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal memperbarui pengaturan: ${e.message}"
            )
        }
    }
    
    fun setCustomDuration(minutes: Int) {
        try {
            if (minutes <= 0) {
                _uiState.value = _uiState.value.copy(
                    error = "Durasi harus lebih dari 0 menit"
                )
                return
            }
            
            val currentState = _uiState.value.pomodoroState
            _uiState.value = _uiState.value.copy(
                pomodoroState = currentState.copy(
                    timeRemaining = minutes * 60
                )
            )
            
            pomodoroService?.setCustomDuration(minutes * 60)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Gagal mengatur durasi: ${e.message}"
            )
        }
    }
    
    fun resetToDefault() {
        resetSession()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshStatistics() {
        loadStatistics()
    }
    
    override fun onCleared() {
        super.onCleared()
        try {
            if (bound) {
                context.unbindService(connection)
                bound = false
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
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