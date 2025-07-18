package com.daustodo.app.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.daustodo.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var isMuted = false
    private var volume = 0.7f
    
    private val vibrator by lazy {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = ContextCompat.getSystemService(context, VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.getSystemService(context, Vibrator::class.java)
        }
    }
    
    fun playWorkCompleteSound() {
        if (!isMuted) {
            playSound(R.raw.bell_sound)
        }
        vibrate(VibrationPattern.WORK_COMPLETE)
    }
    
    fun playBreakCompleteSound() {
        if (!isMuted) {
            playSound(R.raw.break_sound)
        }
        vibrate(VibrationPattern.BREAK_COMPLETE)
    }
    
    fun playTickSound() {
        if (!isMuted) {
            playSound(R.raw.tick_sound)
        }
    }
    
    fun playNotificationSound() {
        if (!isMuted) {
            playSound(R.raw.notification_sound)
        }
        vibrate(VibrationPattern.NOTIFICATION)
    }
    
    private fun playSound(resourceId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                setVolume(volume, volume)
                setOnCompletionListener { mp ->
                    mp.release()
                }
                start()
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing sound: ${e.message}")
        }
    }
    
    private fun vibrate(pattern: VibrationPattern) {
        try {
            vibrator?.let { vib ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createWaveform(pattern.pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(pattern.pattern, -1)
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error vibrating: ${e.message}")
        }
    }
    
    fun setMuted(muted: Boolean) {
        isMuted = muted
    }
    
    fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(this.volume, this.volume)
    }
    
    fun isMuted(): Boolean = isMuted
    
    fun getVolume(): Float = volume
    
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    enum class VibrationPattern(val pattern: LongArray) {
        WORK_COMPLETE(longArrayOf(0, 200, 100, 200, 100, 200)),
        BREAK_COMPLETE(longArrayOf(0, 100, 50, 100)),
        NOTIFICATION(longArrayOf(0, 150)),
        TICK(longArrayOf(0, 50))
    }
}