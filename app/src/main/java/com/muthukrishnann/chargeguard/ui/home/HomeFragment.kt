package com.muthukrishnann.chargeguard.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.muthukrishnann.chargeguard.R
import com.muthukrishnann.chargeguard.alarm.AlarmController
import com.muthukrishnann.chargeguard.data.PreferencesManager
import com.muthukrishnann.chargeguard.databinding.FragmentHomeBinding
import com.muthukrishnann.chargeguard.service.BatteryMonitorService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager(requireContext())

        setupLimitChips()
        setupTestAlarmButton()
        observeBatteryState()
    }

    private fun setupLimitChips() {
        val currentLimit = prefs.chargeLimit
        when (currentLimit) {
            80 -> binding.chip80.isChecked = true
            85 -> binding.chip85.isChecked = true
            90 -> binding.chip90.isChecked = true
            95 -> binding.chip95.isChecked = true
            100 -> binding.chip100.isChecked = true
            else -> binding.chip85.isChecked = true
        }

        binding.chipGroupLimits.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val selectedLimit = when (checkedIds.first()) {
                R.id.chip80 -> 80
                R.id.chip85 -> 85
                R.id.chip90 -> 90
                R.id.chip95 -> 95
                R.id.chip100 -> 100
                else -> 85
            }
            prefs.chargeLimit = selectedLimit
            binding.tvSelectedLimitBadge.text = "Alarm Limit: ${selectedLimit}%"
            Toast.makeText(requireContext(), "Alarm set for ${selectedLimit}%", Toast.LENGTH_SHORT).show()
        }

        binding.tvSelectedLimitBadge.text = "Alarm Limit: ${currentLimit}%"
    }

    private fun setupTestAlarmButton() {
        binding.btnTestAlarm.setOnClickListener {
            if (AlarmController.isAlarmActive.value) {
                AlarmController.stopAlarm(requireContext())
                binding.btnTestAlarm.text = "🔊 Test Alarm"
            } else {
                Toast.makeText(requireContext(), "Testing Alarm for 5 seconds...", Toast.LENGTH_SHORT).show()
                AlarmController.triggerAlarm(
                    requireContext(),
                    isFullCharge = false,
                    currentPercentage = prefs.chargeLimit,
                    limitPercentage = prefs.chargeLimit
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            AlarmController.isAlarmActive.collectLatest { active ->
                if (active) {
                    binding.btnTestAlarm.text = "🛑 Stop Active Alarm"
                } else {
                    binding.btnTestAlarm.text = "🔊 Test Alarm"
                }
            }
        }
    }

    private fun observeBatteryState() {
        viewLifecycleOwner.lifecycleScope.launch {
            BatteryMonitorService.batteryState.collectLatest { info ->
                // Update Circular Indicator
                binding.circularBatteryView.setBatteryData(info.percentage, info.isCharging)

                // Update Home Cards
                binding.tvBatteryPercentage.text = "🔋 ${info.percentage}%"

                if (info.isCharging) {
                    binding.tvChargingStatus.text = "⚡ Charging (${info.chargingType})"
                    binding.tvChargingStatus.setTextColor(resources.getColor(R.color.emerald_400, null))
                } else {
                    binding.tvChargingStatus.text = "🔌 Disconnected"
                    binding.tvChargingStatus.setTextColor(resources.getColor(R.color.slate_400, null))
                }

                binding.tvMonitoringStatus.text = if (info.isMonitoring) "🟢 Monitoring Active" else "🔴 Monitoring Inactive"

                // Diagnostic specs
                binding.tvTemp.text = "${info.temperatureCelsius}°C"
                binding.tvVoltage.text = "${info.voltageVolts} V"
                binding.tvHealth.text = info.health
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
