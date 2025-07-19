package com.daustodo.app.utils

import android.util.Log
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

object PerformanceMonitor {
    private const val TAG = "PerformanceMonitor"
    private val performanceData = mutableMapOf<String, MutableList<Long>>()
    
    fun startTimer(operation: String): Long {
        return System.currentTimeMillis()
    }
    
    fun endTimer(operation: String, startTime: Long) {
        val duration = System.currentTimeMillis() - startTime
        performanceData.getOrPut(operation) { mutableListOf() }.add(duration)
        
        if (duration > 100) { // Log slow operations
            Log.w(TAG, "Slow operation detected: $operation took ${duration}ms")
        }
    }
    
    fun measureOperation(operation: String, block: () -> Unit) {
        val startTime = startTimer(operation)
        try {
            block()
        } finally {
            endTimer(operation, startTime)
        }
    }
    
    suspend fun measureSuspendOperation(operation: String, block: suspend () -> Unit) {
        val startTime = startTimer(operation)
        try {
            block()
        } finally {
            endTimer(operation, startTime)
        }
    }
    
    fun getAverageTime(operation: String): Long {
        val times = performanceData[operation] ?: return 0
        return if (times.isNotEmpty()) times.average().toLong() else 0
    }
    
    fun getPerformanceReport(): String {
        return buildString {
            appendLine("Performance Report:")
            performanceData.forEach { (operation, times) ->
                val avg = times.average()
                val min = times.minOrNull() ?: 0
                val max = times.maxOrNull() ?: 0
                appendLine("$operation: avg=${avg.toLong()}ms, min=${min}ms, max=${max}ms, count=${times.size}")
            }
        }
    }
    
    fun clearData() {
        performanceData.clear()
    }
}

class CacheManager<T>(
    private val maxSize: Int = 100,
    private val defaultExpiryMinutes: Int = 30
) {
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val expiryMinutes: Int
    )
    
    private val cache = mutableMapOf<String, CacheEntry<T>>()
    
    fun put(key: String, data: T, expiryMinutes: Int = defaultExpiryMinutes) {
        if (cache.size >= maxSize) {
            // Remove oldest entry
            val oldestKey = cache.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { cache.remove(it) }
        }
        
        cache[key] = CacheEntry(data, System.currentTimeMillis(), expiryMinutes)
    }
    
    fun get(key: String): T? {
        val entry = cache[key] ?: return null
        
        val expiryTime = entry.timestamp + (entry.expiryMinutes * 60 * 1000)
        if (System.currentTimeMillis() > expiryTime) {
            cache.remove(key)
            return null
        }
        
        return entry.data
    }
    
    fun clear() {
        cache.clear()
    }
    
    fun size(): Int = cache.size
    
    fun removeExpired() {
        val currentTime = System.currentTimeMillis()
        cache.entries.removeIf { (_, entry) ->
            val expiryTime = entry.timestamp + (entry.expiryMinutes * 60 * 1000)
            currentTime > expiryTime
        }
    }
}

object BackgroundTaskManager {
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun executeInBackground(block: suspend () -> Unit) {
        backgroundScope.launch {
            try {
                PerformanceMonitor.measureSuspendOperation("background_task") {
                    block()
                }
            } catch (e: Exception) {
                Log.e("BackgroundTaskManager", "Background task failed", e)
            }
        }
    }
    
    fun executeWithTimeout(
        timeoutMs: Long = 5000,
        block: suspend () -> Unit
    ) {
        backgroundScope.launch {
            try {
                withTimeout(timeoutMs) {
                    PerformanceMonitor.measureSuspendOperation("background_task_with_timeout") {
                        block()
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.w("BackgroundTaskManager", "Background task timed out after ${timeoutMs}ms")
            } catch (e: Exception) {
                Log.e("BackgroundTaskManager", "Background task failed", e)
            }
        }
    }
    
    fun cancelAll() {
        backgroundScope.cancel()
    }
}