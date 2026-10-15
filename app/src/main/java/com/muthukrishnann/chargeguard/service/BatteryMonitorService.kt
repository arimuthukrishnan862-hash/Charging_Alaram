package com.muthukrishnann.chargeguard.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.muthukrishnann.chargeguard.ChargeGuardApp
import com.muthukrishnann.chargeguard.R
import com.muthukrishnann.chargeguard.alarm.AlarmController
import com.muthukrishnann.chargeguard.data.AppDatabase
import com.muthukrishnann.chargeguard.data.ChargingSession
import com.muthukrishnann.chargeguard.data.PreferencesManager
import com.muthukrishnann.chargeguard.ui.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

class BatteryMonitorService : LifecycleService() {

    data class LiveBatteryInfo(
        val percentage: Int = 0,
        val isCharging: Boolean = false,
        val chargingType: String = "Not Charging",
        val temperatureCelsius: Float = 0f,
        val voltageVolts: Float = 0f,
        val health: String = "Good",
        val isMonitoring: Boolean = true
    )

    companion object {
        const val NOTIFICATION_ID_FOREGROUND = 1001
        const val ACTION_START_MONITORING = "ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "ACTION_STOP_MONITORING"

        private val _batteryState = MutableStateFlow(LiveBatteryInfo())
        val batteryState: StateFlow<LiveBatteryInfo> = _batteryState

        fun startService(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            context.startService(intent)
        }
    }

    private lateinit var prefs: PreferencesManager
    private lateinit var database: AppDatabase

    // Session tracking variables
    private var sessionStartTime: Long = 0
    private var sessionStartLevel: Int = -1
    private var sessionMaxLevel: Int = -1
    private var isCurrentlyCharging: Boolean = false

    // Alert tracking to avoid repeat ringing on same percentage
    private var hasAlertedForLimit: Boolean = false
    private var hasAlertedForFull: Boolean = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                processBatteryChangedIntent(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesManager(this)
        database = AppDatabase.getDatabase(this)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.action == ACTION_STOP_MONITORING) {
            prefs.isMonitoringEnabled = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        prefs.isMonitoringEnabled = true
        startForeground(NOTIFICATION_ID_FOREGROUND, buildNotification(_batteryState.value))
        return START_STICKY
    }

    private fun processBatteryChangedIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = if (level >= 0 && scale > 0) (level * 100) / scale else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> if (isCharging) "Charging" else "Unplugged"
        }

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000f

        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val health = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Normal"
        }

        val info = LiveBatteryInfo(
            percentage = percentage,
            isCharging = isCharging,
            chargingType = plugType,
            temperatureCelsius = temp,
            voltageVolts = voltage,
            health = health,
            isMonitoring = true
        )
        _batteryState.value = info

        // Update ongoing foreground notification
        val notification = buildNotification(info)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID_FOREGROUND, notification)

        handleChargingSessionLifecycle(isCharging, percentage)
        evaluateAlarmTriggers(isCharging, percentage)
    }

    private fun handleChargingSessionLifecycle(isCharging: Boolean, currentLevel: Int) {
        if (isCharging && !isCurrentlyCharging) {
            // Charging started
            isCurrentlyCharging = true
            sessionStartTime = System.currentTimeMillis()
            sessionStartLevel = currentLevel
            sessionMaxLevel = currentLevel
            hasAlertedForLimit = false
            hasAlertedForFull = false
        } else if (isCharging && isCurrentlyCharging) {
            // Charging ongoing
            sessionMaxLevel = max(sessionMaxLevel, currentLevel)
        } else if (!isCharging && isCurrentlyCharging) {
            // Charger disconnected
            isCurrentlyCharging = false
            val sessionEndTime = System.currentTimeMillis()
            val durationSeconds = (sessionEndTime - sessionStartTime) / 1000

            // If history is enabled and session lasted at least 15 seconds, save record
            if (prefs.isHistoryEnabled && durationSeconds >= 10 && sessionStartLevel >= 0) {
                val session = ChargingSession(
                    startTime = sessionStartTime,
                    endTime = sessionEndTime,
                    startLevel = sessionStartLevel,
                    endLevel = currentLevel,
                    maxLevel = sessionMaxLevel,
                    durationSeconds = durationSeconds
                )
                lifecycleScope.launch {
                    database.chargingDao().insertSession(session)
                }
            }

            // Stop any ongoing alarm when user unplugs charger
            AlarmController.stopAlarm(this)
            hasAlertedForLimit = false
            hasAlertedForFull = false
        }
    }

    private fun evaluateAlarmTriggers(isCharging: Boolean, currentPercentage: Int) {
        if (!isCharging) return

        val limit = prefs.chargeLimit

        // 1. Full Charge Check (100%)
        if (currentPercentage >= 100) {
            if (!hasAlertedForFull) {
                hasAlertedForFull = true
                AlarmController.triggerAlarm(this, isFullCharge = true, currentPercentage = 100, limitPercentage = 100)
            }
            return
        }

        // 2. Selected Limit Check (e.g. 80%, 85%, 90%, 95%)
        if (currentPercentage >= limit && !hasAlertedForLimit) {
            hasAlertedForLimit = true
            AlarmController.triggerAlarm(this, isFullCharge = false, currentPercentage = currentPercentage, limitPercentage = limit)
        }
    }

    private fun buildNotification(info: LiveBatteryInfo): Notification {
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (info.isCharging) {
            "⚡ Charging: ${info.percentage}% • Limit: ${prefs.chargeLimit}%"
        } else {
            "🔋 Battery: ${info.percentage}% • Unplugged"
        }

        val subText = "ChargeGuard Active (Limit: ${prefs.chargeLimit}%)"

        return NotificationCompat.Builder(this, ChargeGuardApp.CHANNEL_MONITORING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("ChargeGuard Battery Monitor")
            .setContentText(statusText)
            .setSubText(subText)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .build()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}
