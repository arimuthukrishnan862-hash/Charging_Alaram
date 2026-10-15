package com.muthukrishnann.chargeguard.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.muthukrishnann.chargeguard.R
import com.muthukrishnann.chargeguard.data.PreferencesManager
import com.muthukrishnann.chargeguard.databinding.ActivityMainBinding
import com.muthukrishnann.chargeguard.service.BatteryMonitorService
import com.muthukrishnann.chargeguard.ui.history.HistoryFragment
import com.muthukrishnann.chargeguard.ui.home.HomeFragment
import com.muthukrishnann.chargeguard.ui.settings.SettingsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager

    private val homeFragment = HomeFragment()
    private val historyFragment = HistoryFragment()
    private val settingsFragment = SettingsFragment()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Notification permission handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferencesManager(this)

        checkAndRequestPermissions()
        startBackgroundService()
        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun startBackgroundService() {
        if (prefs.isMonitoringEnabled) {
            BatteryMonitorService.startService(this)
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(homeFragment)
                    true
                }
                R.id.nav_history -> {
                    loadFragment(historyFragment)
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
