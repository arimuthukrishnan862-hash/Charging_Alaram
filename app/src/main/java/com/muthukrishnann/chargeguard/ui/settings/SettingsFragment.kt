package com.muthukrishnann.chargeguard.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.muthukrishnann.chargeguard.R
import com.muthukrishnann.chargeguard.data.PreferencesManager
import com.muthukrishnann.chargeguard.databinding.FragmentSettingsBinding
import com.muthukrishnann.chargeguard.service.BatteryMonitorService

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        setupLimitPreference()
        setupToggles()
        setupVolumeSlider()
        setupThemeSelector()
        setupBatteryOptimizationButton()
    }

    private fun setupLimitPreference() {
        val limits = listOf(80, 85, 90, 95, 100)
        val current = prefs.chargeLimit

        binding.radioGroupLimits.check(
            when (current) {
                80 -> R.id.rbLimit80
                85 -> R.id.rbLimit85
                90 -> R.id.rbLimit90
                95 -> R.id.rbLimit95
                100 -> R.id.rbLimit100
                else -> R.id.rbLimit85
            }
        )

        binding.radioGroupLimits.setOnCheckedChangeListener { _, checkedId ->
            val limit = when (checkedId) {
                R.id.rbLimit80 -> 80
                R.id.rbLimit85 -> 85
                R.id.rbLimit90 -> 90
                R.id.rbLimit95 -> 95
                R.id.rbLimit100 -> 100
                else -> 85
            }
            prefs.chargeLimit = limit
            Toast.makeText(requireContext(), "Charge Limit saved: ${limit}%", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupToggles() {
        binding.switchVibration.isChecked = prefs.isVibrationEnabled
        binding.switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.isVibrationEnabled = isChecked
        }

        binding.switchNotifications.isChecked = prefs.isNotificationsEnabled
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.isNotificationsEnabled = isChecked
        }

        binding.switchHistory.isChecked = prefs.isHistoryEnabled
        binding.switchHistory.setOnCheckedChangeListener { _, isChecked ->
            prefs.isHistoryEnabled = isChecked
        }

        binding.switchMonitoring.isChecked = prefs.isMonitoringEnabled
        binding.switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            prefs.isMonitoringEnabled = isChecked
            if (isChecked) {
                BatteryMonitorService.startService(requireContext())
            } else {
                BatteryMonitorService.stopService(requireContext())
            }
        }
    }

    private fun setupVolumeSlider() {
        binding.seekBarVolume.progress = prefs.alarmVolume
        binding.tvVolumeValue.text = "${prefs.alarmVolume}%"

        binding.seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvVolumeValue.text = "$progress%"
                if (fromUser) {
                    prefs.alarmVolume = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupThemeSelector() {
        when (prefs.themeMode) {
            "light" -> binding.rbThemeLight.isChecked = true
            "dark" -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbThemeLight -> "light"
                R.id.rbThemeDark -> "dark"
                else -> "system"
            }
            prefs.themeMode = mode

            when (mode) {
                "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun setupBatteryOptimizationButton() {
        binding.btnBatteryOptimization.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
                val packageName = requireContext().packageName
                if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "ChargeGuard is already exempted from battery optimization!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
