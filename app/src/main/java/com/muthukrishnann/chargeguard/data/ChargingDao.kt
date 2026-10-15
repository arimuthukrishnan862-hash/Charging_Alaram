package com.muthukrishnann.chargeguard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChargingSession): Long

    @Query("SELECT * FROM charging_history ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ChargingSession>>

    @Query("DELETE FROM charging_history")
    suspend fun clearHistory()

    @Delete
    suspend fun deleteSession(session: ChargingSession)
}
