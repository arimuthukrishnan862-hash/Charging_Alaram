package com.muthukrishnann.chargeguard

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.muthukrishnann.chargeguard.data.PreferencesManager

class ChargeGuardApp : Application() {

    companion object {
        const val CHANNEL_MONITORING = "channel_chargeguard_monitoring"
        const val CHANNEL_ALARM = "channel_chargeguard_alarm"
        lateinit var instance: ChargeGuardApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        applyTheme()
        createNotificationChannels()
    }

    private fun applyTheme() {
        val prefs = PreferencesManager(this)
        when (prefs.themeMode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Foreground Monitoring Channel (Silent, persistent)
            val monitoringChannel = NotificationChannel(
                CHANNEL_MONITORING,
                "Battery Monitoring Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time battery status and charging monitor in the background"
                setShowBadge(false)
            }

            // Charging Alarm Channel (High priority, sound, vibration, heads-up)
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM,
                "Charging & Limit Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts you when battery reaches your chosen charging limit or full charge"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 800)
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(alarmSound, audioAttributes)
            }

            notificationManager.createNotificationChannel(monitoringChannel)
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }
}
