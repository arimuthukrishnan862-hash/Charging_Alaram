package com.muthukrishnann.chargeguard.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.muthukrishnann.chargeguard.ChargeGuardApp
import com.muthukrishnann.chargeguard.R
import com.muthukrishnann.chargeguard.data.PreferencesManager
import com.muthukrishnann.chargeguard.ui.alarm.AlarmAlertActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AlarmController {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    private val _isAlarmActive = MutableStateFlow(false)
    val isAlarmActive: StateFlow<Boolean> = _isAlarmActive

    const val NOTIFICATION_ID_ALARM = 2001

    fun triggerAlarm(context: Context, isFullCharge: Boolean, currentPercentage: Int, limitPercentage: Int) {
        if (_isAlarmActive.value) return // already playing

        val prefs = PreferencesManager(context)
        _isAlarmActive.value = true

        val title = if (isFullCharge) {
            "🔋 Battery Fully Charged!"
        } else {
            "🔋 Charging Limit Reached!"
        }

        val message = if (isFullCharge) {
            "Battery reached 100%. Please unplug your charger."
        } else {
            "Battery reached ${currentPercentage}% (Limit: ${limitPercentage}%). Please unplug your charger."
        }

        // 1. Play Alarm Audio
        try {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                val volume = prefs.alarmVolume / 100f
                setVolume(volume, volume)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Trigger Vibration if enabled
        if (prefs.isVibrationEnabled) {
            startVibration(context)
        }

        // 3. Show Heads-up Notification with STOP action
        if (prefs.isNotificationsEnabled) {
            showAlarmNotification(context, title, message)
        }

        // 4. Launch Full-Screen Prominent Alert Activity
        val alertIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(AlarmAlertActivity.EXTRA_IS_FULL_CHARGE, isFullCharge)
            putExtra(AlarmAlertActivity.EXTRA_PERCENTAGE, currentPercentage)
            putExtra(AlarmAlertActivity.EXTRA_LIMIT, limitPercentage)
        }
        context.startActivity(alertIntent)
    }

    private fun startVibration(context: Context) {
        try {
            val pattern = longArrayOf(0, 600, 300, 600, 300, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
                val effect = VibrationEffect.createWaveform(pattern, 0)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(pattern, 0)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, 0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarm(context: Context) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            vibrator?.cancel()
            vibrator = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Cancel notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_ALARM)

        _isAlarmActive.value = false
    }

    private fun showAlarmNotification(context: Context, title: String, message: String) {
        val stopIntent = Intent(context, StopAlarmReceiver::class.java)
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(context, AlarmAlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            1,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ChargeGuardApp.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_stop, "STOP ALARM", stopPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_ALARM, notification)
    }
}
