package com.muthukrishnann.chargeguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_history")
data class ChargingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val startLevel: Int,
    val endLevel: Int,
    val maxLevel: Int,
    val durationSeconds: Long
)
