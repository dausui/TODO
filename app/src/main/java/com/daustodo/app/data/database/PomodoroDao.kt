package com.daustodo.app.data.database

import androidx.room.*
import com.daustodo.app.data.model.PomodoroSession
import com.daustodo.app.data.model.PomodoroType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface PomodoroDao {
    
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    fun getSessionsByTask(taskId: Long): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE isCompleted = 1 ORDER BY startTime DESC")
    fun getCompletedSessions(): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE DATE(startTime) = DATE(:date) ORDER BY startTime DESC")
    fun getSessionsByDate(date: LocalDateTime): Flow<List<PomodoroSession>>
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE taskId = :taskId AND isCompleted = 1 AND type = :type")
    suspend fun getCompletedSessionsCount(taskId: Long, type: PomodoroType): Int
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE isCompleted = 1 AND type = 'WORK' AND DATE(startTime) = DATE(:date)")
    suspend fun getCompletedWorkSessionsToday(date: LocalDateTime): Int
    
    @Insert
    suspend fun insertSession(session: PomodoroSession): Long
    
    @Update
    suspend fun updateSession(session: PomodoroSession)
    
    @Delete
    suspend fun deleteSession(session: PomodoroSession)
    
    @Query("DELETE FROM pomodoro_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
    
    @Query("UPDATE pomodoro_sessions SET isCompleted = :isCompleted, endTime = :endTime WHERE id = :id")
    suspend fun completeSession(id: Long, isCompleted: Boolean, endTime: LocalDateTime)
}