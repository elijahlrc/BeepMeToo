package com.beepmetoo.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.beepmetoo.data.db.entity.ScheduledBeep
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledBeepDao {

    @Insert
    suspend fun insert(beep: ScheduledBeep): Long

    @Query("SELECT * FROM scheduled_beeps WHERE fired = 0 ORDER BY scheduledTime ASC")
    fun getUnfired(): Flow<List<ScheduledBeep>>

    @Query("UPDATE scheduled_beeps SET fired = 1 WHERE id = :id")
    suspend fun markFired(id: Long)

    @Query("DELETE FROM scheduled_beeps WHERE timerProfileId = :profileId")
    suspend fun deleteByProfile(profileId: Long)
}
