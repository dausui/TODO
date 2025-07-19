package com.daustodo.app.data.database

import androidx.room.*
import com.daustodo.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * DAO untuk operasi database Pomodoro dengan query yang jelas dan berguna
 */
@Dao
interface PomodoroDao {
    
    // ==================== CRUD OPERATIONS ====================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSession): Long
    
    @Update
    suspend fun updateSession(session: PomodoroSession)
    
    @Delete
    suspend fun deleteSession(session: PomodoroSession)
    
    @Query("DELETE FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)
    
    // ==================== QUERY OPERATIONS ====================
    
    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): PomodoroSession?
    
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedSessions(): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE status = 'ACTIVE' ORDER BY startedAt DESC")
    fun getActiveSessions(): Flow<List<PomodoroSession>>
    
    // ==================== TASK-BASED QUERIES ====================
    
    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startedAt DESC")
    fun getSessionsForTask(taskId: Long): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedSessionsForTask(taskId: Long): Flow<List<PomodoroSession>>
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE taskId = :taskId AND isCompleted = 1")
    suspend fun getCompletedSessionCountForTask(taskId: Long): Int
    
    // ==================== TYPE-BASED QUERIES ====================
    
    @Query("SELECT * FROM pomodoro_sessions WHERE type = :type ORDER BY startedAt DESC")
    fun getSessionsByType(type: PomodoroType): Flow<List<PomodoroSession>>
    
    @Query("SELECT * FROM pomodoro_sessions WHERE type = :type AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedSessionsByType(type: PomodoroType): Flow<List<PomodoroSession>>
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = :type AND isCompleted = 1")
    suspend fun getCompletedSessionCountByType(type: PomodoroType): Int
    
    // ==================== DATE-BASED QUERIES ====================
    
    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE DATE(startedAt) = DATE(:date) 
        ORDER BY startedAt DESC
    """)
    fun getSessionsStartedOn(date: LocalDateTime): Flow<List<PomodoroSession>>
    
    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE DATE(completedAt) = DATE(:date) 
        AND isCompleted = 1 
        ORDER BY completedAt DESC
    """)
    fun getSessionsCompletedOn(date: LocalDateTime): Flow<List<PomodoroSession>>
    
    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE startedAt BETWEEN :startDate AND :endDate 
        ORDER BY startedAt DESC
    """)
    fun getSessionsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PomodoroSession>>
    
    @Query("""
        SELECT * FROM pomodoro_sessions 
        WHERE completedAt BETWEEN :startDate AND :endDate 
        AND isCompleted = 1 
        ORDER BY completedAt DESC
    """)
    fun getCompletedSessionsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PomodoroSession>>
    
    // ==================== STATISTICS QUERIES ====================
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions")
    suspend fun getTotalSessionCount(): Int
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE isCompleted = 1")
    suspend fun getCompletedSessionCount(): Int
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'WORK' AND isCompleted = 1")
    suspend fun getCompletedWorkSessionCount(): Int
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'WORK' AND isCompleted = 1 AND DATE(completedAt) = DATE(:date)")
    suspend fun getCompletedWorkSessionsToday(date: LocalDateTime = LocalDateTime.now()): Int
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'WORK' AND isCompleted = 1 AND DATE(completedAt) = DATE(:date)")
    suspend fun getCompletedWorkSessionsOnDate(date: LocalDateTime): Int
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'WORK' AND isCompleted = 1 AND completedAt >= :startDate")
    suspend fun getCompletedWorkSessionsSince(startDate: LocalDateTime): Int
    
    @Query("SELECT SUM(duration) FROM pomodoro_sessions WHERE type = 'WORK' AND isCompleted = 1")
    suspend fun getTotalWorkTime(): Int?
    
    @Query("SELECT SUM(duration) FROM pomodoro_sessions WHERE type IN ('SHORT_BREAK', 'LONG_BREAK') AND isCompleted = 1")
    suspend fun getTotalBreakTime(): Int?
    
    @Query("SELECT AVG(duration) FROM pomodoro_sessions WHERE isCompleted = 1")
    suspend fun getAverageSessionDuration(): Float?
    
    @Query("SELECT AVG(actualDuration) FROM pomodoro_sessions WHERE isCompleted = 1 AND actualDuration > 0")
    suspend fun getAverageActualSessionDuration(): Float?
    
    // ==================== PRODUCTIVITY QUERIES ====================
    
    @Query("SELECT AVG(productivityRating) FROM pomodoro_sessions WHERE isCompleted = 1 AND productivityRating > 0")
    suspend fun getAverageProductivityRating(): Float?
    
    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE interruptions > 0")
    suspend fun getInterruptedSessionCount(): Int
    
    @Query("SELECT AVG(interruptions) FROM pomodoro_sessions WHERE isCompleted = 1")
    suspend fun getAverageInterruptions(): Float?
    
    // ==================== STREAK QUERIES ====================
    
    @Query("""
        SELECT COUNT(DISTINCT DATE(completedAt)) as consecutive_days
        FROM pomodoro_sessions 
        WHERE type = 'WORK' 
        AND isCompleted = 1 
        AND completedAt >= (
            SELECT MAX(completedAt) 
            FROM pomodoro_sessions 
            WHERE type = 'WORK' 
            AND isCompleted = 1 
            AND DATE(completedAt) < DATE(:date)
        )
        AND DATE(completedAt) <= DATE(:date)
    """)
    suspend fun getCurrentStreak(date: LocalDateTime = LocalDateTime.now()): Int
    
    @Query("""
        SELECT MAX(consecutive_days) as longest_streak
        FROM (
            SELECT COUNT(DISTINCT DATE(completedAt)) as consecutive_days
            FROM pomodoro_sessions 
            WHERE type = 'WORK' 
            AND isCompleted = 1 
            GROUP BY DATE(completedAt)
        )
    """)
    suspend fun getLongestStreak(): Int?
    
    // ==================== TIME TRACKING QUERIES ====================
    
    @Query("""
        SELECT SUM(actualDuration) 
        FROM pomodoro_sessions 
        WHERE taskId = :taskId 
        AND isCompleted = 1
    """)
    suspend fun getTotalTimeForTask(taskId: Long): Int?
    
    @Query("""
        SELECT SUM(actualDuration) 
        FROM pomodoro_sessions 
        WHERE type = 'WORK' 
        AND isCompleted = 1 
        AND DATE(completedAt) = DATE(:date)
    """)
    suspend fun getTotalWorkTimeToday(date: LocalDateTime = LocalDateTime.now()): Int?
    
    @Query("""
        SELECT SUM(actualDuration) 
        FROM pomodoro_sessions 
        WHERE type = 'WORK' 
        AND isCompleted = 1 
        AND completedAt BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalWorkTimeInRange(startDate: LocalDateTime, endDate: LocalDateTime): Int?
    
    // ==================== BULK OPERATIONS ====================
    
    @Query("UPDATE pomodoro_sessions SET status = :status WHERE id = :sessionId")
    suspend fun updateSessionStatus(sessionId: Long, status: SessionStatus)
    
    @Query("UPDATE pomodoro_sessions SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :sessionId")
    suspend fun updateSessionCompletion(sessionId: Long, isCompleted: Boolean, completedAt: LocalDateTime? = null)
    
    @Query("UPDATE pomodoro_sessions SET actualDuration = :actualDuration WHERE id = :sessionId")
    suspend fun updateSessionActualDuration(sessionId: Long, actualDuration: Int)
    
    @Query("UPDATE pomodoro_sessions SET interruptions = interruptions + 1 WHERE id = :sessionId")
    suspend fun incrementInterruptions(sessionId: Long)
    
    @Query("UPDATE pomodoro_sessions SET pauseDuration = pauseDuration + :pauseMinutes WHERE id = :sessionId")
    suspend fun addPauseDuration(sessionId: Long, pauseMinutes: Int)
    
    @Query("UPDATE pomodoro_sessions SET productivityRating = :rating WHERE id = :sessionId")
    suspend fun updateProductivityRating(sessionId: Long, rating: Int)
    
    @Query("UPDATE pomodoro_sessions SET notes = :notes WHERE id = :sessionId")
    suspend fun updateSessionNotes(sessionId: Long, notes: String)
    
    // ==================== ANALYTICS QUERIES ====================
    
    @Query("""
        SELECT DATE(completedAt) as date, COUNT(*) as count 
        FROM pomodoro_sessions 
        WHERE type = 'WORK' 
        AND isCompleted = 1 
        AND completedAt BETWEEN :startDate AND :endDate 
        GROUP BY DATE(completedAt) 
        ORDER BY date DESC
    """)
    suspend fun getDailySessionTrend(startDate: LocalDateTime, endDate: LocalDateTime): List<DateCount>
    
    @Query("""
        SELECT type, COUNT(*) as count 
        FROM pomodoro_sessions 
        WHERE isCompleted = 1 
        GROUP BY type 
        ORDER BY count DESC
    """)
    suspend fun getSessionTypeDistribution(): List<SessionTypeCount>
    
    @Query("""
        SELECT taskId, COUNT(*) as count 
        FROM pomodoro_sessions 
        WHERE taskId IS NOT NULL 
        AND isCompleted = 1 
        GROUP BY taskId 
        ORDER BY count DESC 
        LIMIT 10
    """)
    suspend fun getTopTasksBySessionCount(): List<TaskSessionCount>
    
    // ==================== CLEANUP OPERATIONS ====================
    
    @Query("DELETE FROM pomodoro_sessions WHERE isCompleted = 1 AND completedAt < datetime('now', '-' || :days || ' days')")
    suspend fun cleanupOldSessions(days: Int = 90)
    
    @Query("DELETE FROM pomodoro_sessions WHERE status = 'CANCELLED' AND startedAt < datetime('now', '-' || :days || ' days')")
    suspend fun cleanupCancelledSessions(days: Int = 30)
}

/**
 * Data classes untuk analytics
 */
data class SessionTypeCount(
    val type: PomodoroType,
    val count: Int
)

data class TaskSessionCount(
    val taskId: Long,
    val count: Int
)