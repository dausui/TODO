package com.daustodo.app.data.repository

import com.daustodo.app.data.database.PomodoroDao
import com.daustodo.app.data.model.PomodoroSession
import com.daustodo.app.data.model.PomodoroType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PomodoroRepository @Inject constructor(
    private val pomodoroDao: PomodoroDao
) {
    
    fun getAllSessions(): Flow<List<PomodoroSession>> = pomodoroDao.getAllSessions()
    
    fun getSessionsByTask(taskId: Long): Flow<List<PomodoroSession>> = pomodoroDao.getSessionsByTask(taskId)
    
    fun getCompletedSessions(): Flow<List<PomodoroSession>> = pomodoroDao.getCompletedSessions()
    
    fun getSessionsByDate(date: LocalDateTime): Flow<List<PomodoroSession>> = pomodoroDao.getSessionsByDate(date)
    
    suspend fun getCompletedSessionsCount(taskId: Long, type: PomodoroType): Int = 
        pomodoroDao.getCompletedSessionsCount(taskId, type)
    
    suspend fun getCompletedWorkSessionsToday(): Int = 
        pomodoroDao.getCompletedWorkSessionsToday(LocalDateTime.now())
    
    suspend fun getTotalCompletedSessions(): Int = 
        pomodoroDao.getTotalCompletedSessions()
    
    suspend fun insertSession(session: PomodoroSession): Long = pomodoroDao.insertSession(session)
    
    suspend fun updateSession(session: PomodoroSession) = pomodoroDao.updateSession(session)
    
    suspend fun deleteSession(session: PomodoroSession) = pomodoroDao.deleteSession(session)
    
    suspend fun deleteSessionById(id: Long) = pomodoroDao.deleteSessionById(id)
    
    suspend fun completeSession(id: Long) = 
        pomodoroDao.completeSession(id, true, LocalDateTime.now())
    
    suspend fun startNewSession(taskId: Long?, type: PomodoroType): Long {
        val session = PomodoroSession(
            taskId = taskId,
            type = type,
            duration = type.defaultDuration,
            startTime = LocalDateTime.now()
        )
        return insertSession(session)
    }
}