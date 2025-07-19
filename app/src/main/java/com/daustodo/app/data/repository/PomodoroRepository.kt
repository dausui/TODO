package com.daustodo.app.data.repository

import com.daustodo.app.data.database.PomodoroDao
import com.daustodo.app.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk operasi Pomodoro dengan logika yang jelas dan fitur yang berguna
 */
@Singleton
class PomodoroRepository @Inject constructor(
    private val pomodoroDao: PomodoroDao
) {
    
    // ==================== CRUD OPERATIONS ====================
    
    /**
     * Menambah sesi Pomodoro baru
     */
    suspend fun addSession(session: PomodoroSession): Long {
        return pomodoroDao.insertSession(session)
    }
    
    /**
     * Memperbarui sesi Pomodoro
     */
    suspend fun updateSession(session: PomodoroSession) {
        pomodoroDao.updateSession(session)
    }
    
    /**
     * Menghapus sesi Pomodoro
     */
    suspend fun deleteSession(session: PomodoroSession) {
        pomodoroDao.deleteSession(session)
    }
    
    /**
     * Mendapatkan sesi berdasarkan ID
     */
    suspend fun getSessionById(sessionId: Long): PomodoroSession? {
        return pomodoroDao.getSessionById(sessionId)
    }
    
    // ==================== QUERY OPERATIONS ====================
    
    /**
     * Mendapatkan semua sesi
     */
    fun getAllSessions(): Flow<List<PomodoroSession>> {
        return pomodoroDao.getAllSessions()
    }
    
    /**
     * Mendapatkan sesi yang sudah selesai
     */
    fun getCompletedSessions(): Flow<List<PomodoroSession>> {
        return pomodoroDao.getCompletedSessions()
    }
    
    /**
     * Mendapatkan sesi aktif
     */
    fun getActiveSessions(): Flow<List<PomodoroSession>> {
        return pomodoroDao.getActiveSessions()
    }
    
    // ==================== TASK-BASED QUERIES ====================
    
    /**
     * Mendapatkan sesi untuk tugas tertentu
     */
    fun getSessionsForTask(taskId: Long): Flow<List<PomodoroSession>> {
        return pomodoroDao.getSessionsForTask(taskId)
    }
    
    /**
     * Mendapatkan sesi yang selesai untuk tugas tertentu
     */
    fun getCompletedSessionsForTask(taskId: Long): Flow<List<PomodoroSession>> {
        return pomodoroDao.getCompletedSessionsForTask(taskId)
    }
    
    /**
     * Mendapatkan jumlah sesi yang selesai untuk tugas tertentu
     */
    suspend fun getCompletedSessionCountForTask(taskId: Long): Int {
        return pomodoroDao.getCompletedSessionCountForTask(taskId)
    }
    
    // ==================== TYPE-BASED QUERIES ====================
    
    /**
     * Mendapatkan sesi berdasarkan jenis
     */
    fun getSessionsByType(type: PomodoroType): Flow<List<PomodoroSession>> {
        return pomodoroDao.getSessionsByType(type)
    }
    
    /**
     * Mendapatkan sesi yang selesai berdasarkan jenis
     */
    fun getCompletedSessionsByType(type: PomodoroType): Flow<List<PomodoroSession>> {
        return pomodoroDao.getCompletedSessionsByType(type)
    }
    
    /**
     * Mendapatkan jumlah sesi yang selesai berdasarkan jenis
     */
    suspend fun getCompletedSessionCountByType(type: PomodoroType): Int {
        return pomodoroDao.getCompletedSessionCountByType(type)
    }
    
    // ==================== DATE-BASED QUERIES ====================
    
    /**
     * Mendapatkan sesi yang dimulai pada tanggal tertentu
     */
    fun getSessionsStartedOn(date: LocalDateTime): Flow<List<PomodoroSession>> {
        return pomodoroDao.getSessionsStartedOn(date)
    }
    
    /**
     * Mendapatkan sesi yang selesai pada tanggal tertentu
     */
    fun getSessionsCompletedOn(date: LocalDateTime): Flow<List<PomodoroSession>> {
        return pomodoroDao.getSessionsCompletedOn(date)
    }
    
    /**
     * Mendapatkan sesi dalam rentang tanggal
     */
    fun getSessionsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PomodoroSession>> {
        return pomodoroDao.getSessionsInDateRange(startDate, endDate)
    }
    
    /**
     * Mendapatkan sesi yang selesai dalam rentang tanggal
     */
    fun getCompletedSessionsInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<PomodoroSession>> {
        return pomodoroDao.getCompletedSessionsInDateRange(startDate, endDate)
    }
    
    // ==================== STATISTICS OPERATIONS ====================
    
    /**
     * Mendapatkan statistik Pomodoro
     */
    suspend fun getPomodoroStatistics(): PomodoroStatistics {
        val totalSessions = pomodoroDao.getTotalSessionCount()
        val completedSessions = pomodoroDao.getCompletedSessionCount()
        val totalWorkTime = pomodoroDao.getTotalWorkTime() ?: 0
        val totalBreakTime = pomodoroDao.getTotalBreakTime() ?: 0
        val averageSessionLength = pomodoroDao.getAverageSessionDuration()?.toInt() ?: 0
        val todaySessions = pomodoroDao.getCompletedWorkSessionsToday()
        val thisWeekSessions = getCompletedWorkSessionsThisWeek()
        val thisMonthSessions = getCompletedWorkSessionsThisMonth()
        val longestStreak = pomodoroDao.getLongestStreak() ?: 0
        val currentStreak = pomodoroDao.getCurrentStreak()
        val averageProductivityRating = pomodoroDao.getAverageProductivityRating() ?: 0f
        
        val completionRate = if (totalSessions > 0) completedSessions.toFloat() / totalSessions else 0f
        val productivityScore = calculateProductivityScore(
            completionRate,
            averageProductivityRating,
            currentStreak,
            todaySessions
        )
        
        return PomodoroStatistics(
            totalSessions = totalSessions,
            completedSessions = completedSessions,
            totalWorkTime = totalWorkTime,
            totalBreakTime = totalBreakTime,
            averageSessionLength = averageSessionLength,
            completionRate = completionRate,
            todaySessions = todaySessions,
            thisWeekSessions = thisWeekSessions,
            thisMonthSessions = thisMonthSessions,
            productivityScore = productivityScore,
            longestStreak = longestStreak,
            currentStreak = currentStreak
        )
    }
    
    /**
     * Mendapatkan sesi kerja yang selesai hari ini
     */
    suspend fun getCompletedWorkSessionsToday(): Int {
        return pomodoroDao.getCompletedWorkSessionsToday()
    }
    
    /**
     * Mendapatkan sesi kerja yang selesai minggu ini
     */
    suspend fun getCompletedWorkSessionsThisWeek(): Int {
        val now = LocalDateTime.now()
        val startOfWeek = now.toLocalDate().minusDays(now.dayOfWeek.value.toLong() - 1).atStartOfDay()
        return pomodoroDao.getCompletedWorkSessionsSince(startOfWeek)
    }
    
    /**
     * Mendapatkan sesi kerja yang selesai bulan ini
     */
    suspend fun getCompletedWorkSessionsThisMonth(): Int {
        val now = LocalDateTime.now()
        val startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
        return pomodoroDao.getCompletedWorkSessionsSince(startOfMonth)
    }
    
    /**
     * Mendapatkan total waktu kerja hari ini
     */
    suspend fun getTotalWorkTimeToday(): Int {
        return pomodoroDao.getTotalWorkTimeToday() ?: 0
    }
    
    /**
     * Mendapatkan total waktu kerja dalam rentang tanggal
     */
    suspend fun getTotalWorkTimeInRange(startDate: LocalDateTime, endDate: LocalDateTime): Int {
        return pomodoroDao.getTotalWorkTimeInRange(startDate, endDate) ?: 0
    }
    
    /**
     * Mendapatkan total waktu untuk tugas tertentu
     */
    suspend fun getTotalTimeForTask(taskId: Long): Int {
        return pomodoroDao.getTotalTimeForTask(taskId) ?: 0
    }
    
    // ==================== BULK OPERATIONS ====================
    
    /**
     * Memperbarui status sesi
     */
    suspend fun updateSessionStatus(sessionId: Long, status: SessionStatus) {
        pomodoroDao.updateSessionStatus(sessionId, status)
    }
    
    /**
     * Memperbarui penyelesaian sesi
     */
    suspend fun updateSessionCompletion(sessionId: Long, isCompleted: Boolean, completedAt: LocalDateTime? = null) {
        pomodoroDao.updateSessionCompletion(sessionId, isCompleted, completedAt)
    }
    
    /**
     * Memperbarui durasi aktual sesi
     */
    suspend fun updateSessionActualDuration(sessionId: Long, actualDuration: Int) {
        pomodoroDao.updateSessionActualDuration(sessionId, actualDuration)
    }
    
    /**
     * Menambah interupsi
     */
    suspend fun incrementInterruptions(sessionId: Long) {
        pomodoroDao.incrementInterruptions(sessionId)
    }
    
    /**
     * Menambah durasi jeda
     */
    suspend fun addPauseDuration(sessionId: Long, pauseMinutes: Int) {
        pomodoroDao.addPauseDuration(sessionId, pauseMinutes)
    }
    
    /**
     * Memperbarui rating produktivitas
     */
    suspend fun updateProductivityRating(sessionId: Long, rating: Int) {
        pomodoroDao.updateProductivityRating(sessionId, rating)
    }
    
    /**
     * Memperbarui catatan sesi
     */
    suspend fun updateSessionNotes(sessionId: Long, notes: String) {
        pomodoroDao.updateSessionNotes(sessionId, notes)
    }
    
    // ==================== ANALYTICS OPERATIONS ====================
    
    /**
     * Mendapatkan tren sesi harian
     */
    suspend fun getDailySessionTrend(startDate: LocalDateTime, endDate: LocalDateTime): List<DateCount> {
        return pomodoroDao.getDailySessionTrend(startDate, endDate)
    }
    
    /**
     * Mendapatkan distribusi jenis sesi
     */
    suspend fun getSessionTypeDistribution(): List<SessionTypeCount> {
        return pomodoroDao.getSessionTypeDistribution()
    }
    
    /**
     * Mendapatkan tugas teratas berdasarkan jumlah sesi
     */
    suspend fun getTopTasksBySessionCount(): List<TaskSessionCount> {
        return pomodoroDao.getTopTasksBySessionCount()
    }
    
    // ==================== PRODUCTIVITY OPERATIONS ====================
    
    /**
     * Mendapatkan rata-rata rating produktivitas
     */
    suspend fun getAverageProductivityRating(): Float {
        return pomodoroDao.getAverageProductivityRating() ?: 0f
    }
    
    /**
     * Mendapatkan jumlah sesi yang terinterupsi
     */
    suspend fun getInterruptedSessionCount(): Int {
        return pomodoroDao.getInterruptedSessionCount()
    }
    
    /**
     * Mendapatkan rata-rata interupsi
     */
    suspend fun getAverageInterruptions(): Float {
        return pomodoroDao.getAverageInterruptions() ?: 0f
    }
    
    /**
     * Mendapatkan streak saat ini
     */
    suspend fun getCurrentStreak(): Int {
        return pomodoroDao.getCurrentStreak()
    }
    
    /**
     * Mendapatkan streak terpanjang
     */
    suspend fun getLongestStreak(): Int {
        return pomodoroDao.getLongestStreak() ?: 0
    }
    
    // ==================== UTILITY OPERATIONS ====================
    
    /**
     * Menghitung skor produktivitas
     */
    private fun calculateProductivityScore(
        completionRate: Float,
        averageProductivityRating: Float,
        currentStreak: Int,
        todaySessions: Int
    ): Float {
        val completionScore = completionRate * 30 // 30% weight
        val ratingScore = (averageProductivityRating / 5f) * 25 // 25% weight
        val streakScore = minOf(currentStreak * 2f, 25f) // 25% weight, max 25
        val todayScore = minOf(todaySessions * 4f, 20f) // 20% weight, max 20
        
        return completionScore + ratingScore + streakScore + todayScore
    }
    
    /**
     * Mendapatkan sesi untuk hari ini
     */
    fun getTodaySessions(): Flow<List<PomodoroSession>> {
        val today = LocalDateTime.now()
        return pomodoroDao.getSessionsCompletedOn(today)
    }
    
    /**
     * Mendapatkan sesi untuk minggu ini
     */
    fun getThisWeekSessions(): Flow<List<PomodoroSession>> {
        val now = LocalDateTime.now()
        val startOfWeek = now.toLocalDate().minusDays(now.dayOfWeek.value.toLong() - 1).atStartOfDay()
        val endOfWeek = startOfWeek.plusDays(7).minusSeconds(1)
        return pomodoroDao.getCompletedSessionsInDateRange(startOfWeek, endOfWeek)
    }
    
    /**
     * Mendapatkan sesi untuk bulan ini
     */
    fun getThisMonthSessions(): Flow<List<PomodoroSession>> {
        val now = LocalDateTime.now()
        val startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
        val endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1)
        return pomodoroDao.getCompletedSessionsInDateRange(startOfMonth, endOfMonth)
    }
    
    // ==================== CLEANUP OPERATIONS ====================
    
    /**
     * Membersihkan sesi lama
     */
    suspend fun cleanupOldSessions(days: Int = 90) {
        pomodoroDao.cleanupOldSessions(days)
    }
    
    /**
     * Membersihkan sesi yang dibatalkan
     */
    suspend fun cleanupCancelledSessions(days: Int = 30) {
        pomodoroDao.cleanupCancelledSessions(days)
    }
}