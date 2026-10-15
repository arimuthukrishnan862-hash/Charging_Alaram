package com.muthukrishnann.chargeguard.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("chargeguard_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CHARGE_LIMIT = "charge_limit"
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_HISTORY_ENABLED = "history_enabled"
        private const val KEY_ALARM_VOLUME = "alarm_volume"
        private const val KEY_ALARM_SOUND = "alarm_sound"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LAST_ALERT_LEVEL = "last_alert_level"
    }

    var chargeLimit: Int
        get() = prefs.getInt(KEY_CHARGE_LIMIT, 85)
        set(value) = prefs.edit().putInt(KEY_CHARGE_LIMIT, value).apply()

    var isMonitoringEnabled: Boolean
        get() = prefs.getBoolean(KEY_MONITORING_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MONITORING_ENABLED, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var isHistoryEnabled: Boolean
        get() = prefs.getBoolean(KEY_HISTORY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HISTORY_ENABLED, value).apply()

    var alarmVolume: Int
        get() = prefs.getInt(KEY_ALARM_VOLUME, 90)
        set(value) = prefs.edit().putInt(KEY_ALARM_VOLUME, value).apply()

    var alarmSound: String
        get() = prefs.getString(KEY_ALARM_SOUND, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_ALARM_SOUND, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var lastAlertedLevel: Int
        get() = prefs.getInt(KEY_LAST_ALERT_LEVEL, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_ALERT_LEVEL, value).apply()
}
