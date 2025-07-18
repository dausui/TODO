package com.daustodo.app.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object TimeUtils {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
    
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
    
    fun formatDuration(minutes: Int): String {
        return when {
            minutes < 60 -> "${minutes}m"
            minutes % 60 == 0 -> "${minutes / 60}h"
            else -> "${minutes / 60}h ${minutes % 60}m"
        }
    }
    
    fun formatDate(dateTime: LocalDateTime): String {
        return dateTime.format(dateFormatter)
    }
    
    fun formatTime(dateTime: LocalDateTime): String {
        return dateTime.format(timeFormatter)
    }
    
    fun formatDateTime(dateTime: LocalDateTime): String {
        return dateTime.format(dateTimeFormatter)
    }
    
    fun formatRelativeTime(dateTime: LocalDateTime): String {
        val now = LocalDateTime.now()
        val minutes = ChronoUnit.MINUTES.between(dateTime, now)
        
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            minutes < 2880 -> "Yesterday"
            minutes < 43200 -> "${minutes / 1440}d ago"
            else -> formatDate(dateTime)
        }
    }
    
    fun isToday(dateTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        return dateTime.toLocalDate() == now.toLocalDate()
    }
    
    fun isYesterday(dateTime: LocalDateTime): Boolean {
        val now = LocalDateTime.now()
        return dateTime.toLocalDate() == now.toLocalDate().minusDays(1)
    }
    
    fun secondsToMinutes(seconds: Int): Int {
        return seconds / 60
    }
    
    fun minutesToSeconds(minutes: Int): Int {
        return minutes * 60
    }
    
    fun getProgressPercentage(timeRemaining: Int, totalTime: Int): Float {
        if (totalTime == 0) return 0f
        return (totalTime - timeRemaining).toFloat() / totalTime.toFloat()
    }
}