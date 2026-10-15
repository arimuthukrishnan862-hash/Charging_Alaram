package com.muthukrishnann.chargeguard.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.muthukrishnann.chargeguard.alarm.AlarmController
import com.muthukrishnann.chargeguard.databinding.ActivityAlarmAlertBinding

class AlarmAlertActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_IS_FULL_CHARGE = "extra_is_full_charge"
        const val EXTRA_PERCENTAGE = "extra_percentage"
        const val EXTRA_LIMIT = "extra_limit"
    }

    private lateinit var binding: ActivityAlarmAlertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeAndUnlockScreen()

        binding = ActivityAlarmAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isFull = intent.getBooleanExtra(EXTRA_IS_FULL_CHARGE, false)
        val percentage = intent.getIntExtra(EXTRA_PERCENTAGE, 85)
        val limit = intent.getIntExtra(EXTRA_LIMIT, 85)

        if (isFull) {
            binding.tvAlarmTitle.text = "🔋 Battery Fully Charged!"
            binding.tvAlarmSubtitle.text = "Your phone has reached 100% capacity."
            binding.tvAlarmMessage.text = "Please unplug your charger to protect battery health and avoid heat degradation."
        } else {
            binding.tvAlarmTitle.text = "🔋 Charging Limit Reached!"
            binding.tvAlarmSubtitle.text = "Battery reached ${percentage}% (Limit: ${limit}%)"
            binding.tvAlarmMessage.text = "Please unplug your charger now to preserve optimal lithium-ion longevity."
        }

        binding.btnStopAlarm.setOnClickListener {
            AlarmController.stopAlarm(this)
            finish()
        }
    }

    private fun wakeAndUnlockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    override fun onBackPressed() {
        AlarmController.stopAlarm(this)
        super.onBackPressed()
    }
}
