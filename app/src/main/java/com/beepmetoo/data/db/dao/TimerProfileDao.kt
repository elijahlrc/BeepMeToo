package com.beepmetoo.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.beepmetoo.data.db.entity.TimerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerProfileDao {

    @Insert
    suspend fun insert(profile: TimerProfile): Long

    @Update
    suspend fun update(profile: TimerProfile)

    @Delete
    suspend fun delete(profile: TimerProfile)

    @Query("SELECT * FROM timer_profiles WHERE id = :id")
    suspend fun getById(id: Long): TimerProfile?

    @Query("SELECT * FROM timer_profiles WHERE isActive = 1 LIMIT 1")
    fun getActive(): Flow<TimerProfile?>

    @Transaction
    suspend fun setActive(id: Long) {
        deactivateAll()
        activate(id)
    }

    @Query("UPDATE timer_profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE timer_profiles SET isActive = 1 WHERE id = :id")
    suspend fun activate(id: Long)
}
